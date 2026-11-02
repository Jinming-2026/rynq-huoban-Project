package com.yz.minded.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.common.reflect.TypeToken;
import com.yz.minded.common.DefaultUserProps;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.enums.EmailTemplateEnum;
import com.yz.minded.pojo.request.UpdatePwdByEmailRequest;
import com.yz.minded.pojo.request.UpdatePwdRequest;
import com.yz.minded.pojo.request.UserRegisterRequest;
import com.yz.minded.service.FollowRelationshipService;
import com.yz.minded.service.UserService;
import com.yz.minded.mapper.UserMapper;
import com.yz.minded.utils.AlOssUtil;
import com.yz.minded.utils.AlgorithmUtils;
import com.yz.minded.utils.EmailUtils;
import com.yz.minded.utils.StringValidatorUtils;
import io.swagger.annotations.Api;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yz.minded.contant.UserConstant.ADMIN_ROLE;
import static com.yz.minded.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类（处理业务逻辑）
 * @author yz
 */
@Service
@Slf4j
//说明接口文件
@Api(value = "测试接口", tags = "用户管理相关的接口")
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmailUtils emailUtils;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;    // 引入 Redis

    private static final String SALT = "abcd";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 在 UserServiceImpl 里加一个私有静态内部类
    private record UserDistance(User user, long distance) {}

    /**
     * 用户注册
     *
     * @param userRegisterRequest r
     * @return 新用户注册的 id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 获取参数
        String username = userRegisterRequest.getUsername();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String email = userRegisterRequest.getEmail();
        // 1、校验
        // 使用 commons 插件进行一次性判断多个字符串是否为空
        if (StringUtils.isAnyBlank(username, userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容为空");
        }
        // 昵称长度 2 - 10
        if (username.length() < 2 || username.length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称长度为 2-10 位");
        }
        // 1.2 用户账户不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能小于4位");
        }
        // 效验账号
        String userAccountValid = StringValidatorUtils.isValid(false, userAccount);
        if (userAccountValid != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, userAccountValid);
        }
        // 效验密码
        String passwordValid = StringValidatorUtils.isValid(true, userPassword);
        if (passwordValid != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,passwordValid);
        }
        // 判断两次密码输入是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        // 效验邮箱
        boolean validEmail = StringValidatorUtils.isValidEmail(email);
        if (!validEmail) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 查询数据表 userAccount 列里面有没有和 userAccount 相同的内容
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);  // 获取进行查询得到的结果
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前账号已经被注册");
        }

        // 2、 对密码进行加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3、向数据库插入数据
        User userCenter = new User();
        userCenter.setUsername(username);
        userCenter.setUserAccount(userAccount);
        userCenter.setUserPassword(newPassword);
        userCenter.setEmail(email);
        // 设置用户默认数据
        userCenter.setAvatarUrl(DefaultUserProps.AVATAR_URL);
        userCenter.setProfile(DefaultUserProps.PROFILE);

        int saveResult = userMapper.insert(userCenter);
        if (saveResult != 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }

        return userCenter.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      session cookie
     * @return 登录成功用户的信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1、校验
        // 使用 commons 插件进行一次性判断多个字符串是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容为空");
        }
        // 账号规定长度不能小于 4 位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度小于4位");
        }
        // 效验账号
        String userAccountValid = StringValidatorUtils.isValid(false, userAccount);
        if (userAccountValid != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号" + userAccountValid);
        }
        // 效验密码
        String passwordValid = StringValidatorUtils.isValid(true, userPassword);
        if (passwordValid != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码" + passwordValid);
        }
        // 2、 对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User userAccountInfo = userMapper.selectOne(queryWrapper);
        if (userAccountInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前用户不存在");
        }
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 密码错误
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 3、用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4、记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        return safetyUser;
    }

    /**
     * 用户修改信息
     *
     * @param user      修改内容
     * @param loginUser 当前用户的信息
     * @return 1/0
     */
    @Override
    public Integer updateUser(User user, User loginUser) {
        // 参数效验
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，则可以修改所有用户
        // 如果不是管理员，只允许修改自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 更改用户密码
     *
     * @param pwdRequest 传入的密码参数
     * @param loginUser  用户信息
     * @return 修改成功
     */
    @Override
    public Integer updateByPwd(UpdatePwdRequest pwdRequest, User loginUser) {
        if (pwdRequest.getOldPassword() == null || pwdRequest.getNewPassword() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户原始密码,判断用户传入密码是否和数据库中密码相匹配
        User user = userMapper.selectById(loginUser.getId());
        String userPassword = user.getUserPassword();
        // 传入的密码进行加密
        String oldPassword = pwdRequest.getOldPassword();
        String oldPasswordMd5 = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!userPassword.equals(oldPasswordMd5)) {
            // 密码不对
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        // 获取需要更改的密码
        String newPassword = pwdRequest.getNewPassword();
        String checkPassword = pwdRequest.getCheckPassword();
        // 效验密码
        String valid = StringValidatorUtils.isValid(true, newPassword);
        if (valid != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码" + valid);
        }
        // 效验密码是否一致
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        // 对新密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        // 若修改的密码和原密码一样则不进行修改
        if (userPassword.equals(encryptPassword)) {
            return 1;
        }
        // 修改密码
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", loginUser.getId()).set("userPassword", encryptPassword);
        return userMapper.update(null, updateWrapper);
    }

    /**
     * 用户上传头像
     *
     * @param file      头像
     * @param loginUser 登录用户信息
     * @return 1
     */
    @Override
    public String uploadUserAvatarUrl(MultipartFile file, User loginUser) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未上传头像");
        }
        // 获取文件的原始名
        String originalFilename = file.getOriginalFilename();
        // 截取文件原始名的后缀
        String substring = "";
        if (StringUtils.isNotBlank(originalFilename)) {
            substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            return "头像上传失败";
        }
        // 构造新文件名称
        String fileName = UUID.randomUUID() + substring;
        // 调用上传工具类（文件请求路径）
        String fileUrl = AlOssUtil.uploadImage(file, fileName);
        // 将上传文件的 url 地址更新到数据库
        User newUser = new User();
        BeanUtils.copyProperties(loginUser, newUser);
        newUser.setAvatarUrl(fileUrl);
        userMapper.updateById(newUser);
        return "头像上传成功";
    }

    /**
     * 推荐用户
     *
     * @param num       推荐数量
     * @param pageSize  s
     * @param pageNum   n
     * @param loginUser 登录用户
     * @return 推荐用户列表
     */
    @Override
    public Page<User> matchUsers(long num, long pageSize, long pageNum, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        queryWrapper.ne("id", loginUser.getId()); // 不匹配当前登录用户
        // 获取所有标签不为空的用户的 id、tags 字段
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags(); // 当前登录用户的标签（json格式）
        if (StringUtils.isBlank(tags)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将 String 类型的 json 格式标签转为标签集合
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<UserDistance> list = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            // 排除没有标签的用户和自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new UserDistance(user, distance));
        }
        // 按照编辑距离由小到大排序，取出前 num 条数据
        List<UserDistance> topUserPairList = list.stream()
                .sorted((a, b) -> Long.compare(a.distance(), b.distance()))
                .limit(num)
                .toList();
        // 匹配的用户 id 集合（排序好的）
        List<Long> matchUserIdList = topUserPairList.stream()
                .map(pair -> pair.user().getId()).collect(Collectors.toList());
        // 根据 id 集合获取所有用户信息（未排序）
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", matchUserIdList);
        // key对应用户id value对应用户对象集合
        Map<Long, List<User>> safeMatchUserList = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> lastUserList = new ArrayList<>();
        for (Long userId : matchUserIdList) {
            List<User> users = safeMatchUserList.get(userId);
            if (users != null && !users.isEmpty()) {
                lastUserList.add(users.get(0));
            }
        }

        // 分页
        int total = lastUserList.size();
        if (pageNum * pageSize >= total) { // 避免空指针
            lastUserList = lastUserList.subList((int) (pageSize * (pageNum - 1)), total);
        } else {
            lastUserList = lastUserList.subList((int) (pageSize * (pageNum - 1)), (int) (pageNum * pageSize));
        }
        Page<User> userListPage = new Page<>();
        userListPage.setTotal(total);
        userListPage.setPages((long) Math.ceil((double) total / pageSize));
        userListPage.setSize(pageSize);
        userListPage.setRecords(lastUserList);
        userListPage.setCurrent(pageNum);
        return userListPage;
    }

    /**
     * 用户脱敏
     *
     * @param originUser 数据库查询的用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setProfile(originUser.getProfile());
        return safetyUser;
    }

    /**
     * 用户注销（移除session）
     *
     * @param request request
     * @return 1
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 判断是否为管理员
     *
     * @param request 登录用户的session
     * @return true：管理员 false：普通用户
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        if (request == null) return false;
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!(userObj instanceof User user)) return false;
        return user.getUserRole() != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断是否为管理员
     *
     * @return true：管理员 false：普通用户
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取当前用户信息
     *
     * @param request 当前用户的 Cookie
     * @return 当前用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    /**
     * 搜索用户
     *
     * @param username 用户账号
     * @return 用户列表
     */
    @Override
    public List<User> searchUsers(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(user -> {
            user.setUserPassword(null);
            return getSafetyUser(user);
        }).collect(Collectors.toList());
    }


    /**
     * 根据标签搜索用户（SQL版）
     *
     * @param tagList 标签列表
     * @return 用户列表
     */
    @Override
    public List<User> SQLSearchUsersByTags(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求标签列表为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 使用循环代替 and 连接
        // like "Java" and like "C++";
        for (String tagName : tagList) {
            queryWrapper.like("tags", tagName);
        }
        List<User> users = userMapper.selectList(queryWrapper);
        // 将 users 转换成一个 stream 流
        // map 方法中将每个 user 对象转换成 safetyUser 进行用户信息脱敏
        // collect 方法将处理后的结果再次收集到一个新的 List 集合中
        // 以下两种方法均可
        /* return users.stream().map(user -> {
            user.setUserPassword(null);
            return getSafetyUser(user);
        }).collect(Collectors.toList());*/
        return users.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户（内存过滤）
     *
     * @param tagList 标签列表
     * @return 用户列表
     */
    @Override
    public List<User> SearchUsersByTags(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求标签列表为空");
        }
        // 1、查询所有用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        // 2、在所有用户中查询符合要求的用户
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            // 将 Json 序列化转换成 Java 对象
            Set<String> tagsNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            if (tagsNameSet.isEmpty()) {
                tagsNameSet = new HashSet<>();
            }
            for (String tagName : tagList) {
                if (tagsNameSet.contains(tagName)) {
                    return true;
                }
            }
            return false;
        }).map(this::getSafetyUser).collect(Collectors.toList());

    }

    /**
     * 主页展示用户列表 ( Redis )
     *
     * @param pageSize 每一页展示的用户数量
     * @param pageNum  当前的页码
     * @param request  当前登录用户的 request
     * @return 分页查询出来的用户列表
     */
    @Override
    public Page<User> recommendUser(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request); //先获取登录用户信息
        String redisKey = String.format("minded:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
//        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        // 先在缓存中进行查询，如果有缓存，直接读取缓存
        Page<User> userPage;
        List<User> userList;
        userList = (List<User>) valueOperations.get(redisKey);
        if (userList != null) {
            userPage = this.doPage(userList, pageNum, pageSize);
            return userPage;
        }
        // 无缓存，则进行查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id", loginUser.getId()); // 排除自己
        queryWrapper.orderByDesc("updateTime");
        userList = this.list(queryWrapper);
        // 用户信息脱敏
        List<User> safetyUser = userList.stream().map(user -> {
            user.setUserPassword(null);
            user.setPhone(null);
            return getSafetyUser(user);
        }).toList();
        // 对查出的数据，进行写缓存
        try {
            // timeout: 30000 -> 30s （redis 过期时间） TODO 合理设置缓存过期时间
            valueOperations.set(redisKey, safetyUser, 60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Redis set key error", e);
        }
        return this.doPage(safetyUser, pageNum, pageSize);
    }

    /**
     * 分页
     *
     * @param userList 总表
     * @param pageNum  当前页码
     * @param pageSize 页展示量
     * @return 分页结果
     */
    @Override
    public Page<User> doPage(List<User> userList, long pageNum, long pageSize) {
        int totalItem = userList.size();
        int totalPage = (int) Math.ceil((double) totalItem / pageSize);
        Page<User> userPage = new Page<>();
        // 确保页码在有效范围内
        if (pageNum < 1 || pageNum > totalPage) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long start = (pageNum - 1) * pageSize;
        long end = Math.min(pageNum * pageSize, totalItem);
        List<User> userListPage = userList.subList((int) start, (int) end);
        userPage.setCurrent(pageNum);
        userPage.setSize(pageSize);
        userPage.setTotal(totalItem);
        userPage.setRecords(userListPage);
        return userPage;
    }

    /**
     * 获取对象用户的信息
     *
     * @param id 对象用户的id
     * @return 对象用户的信息
     */
    @Override
    public User getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = this.getById(id);
        return this.getSafetyUser(user);
    }

    @Transactional
    @Override
    public String sendMailCaptcha(UpdatePwdByEmailRequest emailRequest) {
        String userAccount = emailRequest.getUserAccount(); // 用户账号
        String email = emailRequest.getEmail();     // 用户邮箱
        // 效验邮箱规则
        boolean validEmail = StringValidatorUtils.isValidEmail(email);
        if (!validEmail) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱不合法");
        }
        // 效验用户数据库中的邮箱和发送邮箱是否一致
        User user = this.getUserByAccount(userAccount);
        if (! user.getEmail().equals(email)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"输入的邮箱与账号绑定的不一致");
        }
        String redisKey = "login:" + userAccount + ":email:captcha:" + email;
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(redisKey);
        // 初始检查
        String lastSendTimestamp = hashOps.get("lastSendTime");
        String sendCount = hashOps.get("sendCount");
        // 倘若发送的次数达到了5次还要发送，那么就封禁一天不让发送短信。
        String banKey = redisKey + ":ban";
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(banKey))) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送过于频繁，请24小时后再试");
        }
        if (StringUtils.isNotBlank(sendCount) && Integer.parseInt(sendCount) >= 5) {
            stringRedisTemplate.opsForValue().set(banKey, "1", 24, TimeUnit.HOURS);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送过于频繁，请24小时后再试");
        }
        if (StringUtils.isNotBlank(lastSendTimestamp)) {
            long lastSendTime = Long.parseLong(lastSendTimestamp);
            long currentTime = System.currentTimeMillis();
            long checkTime = currentTime - lastSendTime;
            // 发送时间间隔 60s
            if (checkTime < 60 * 1000) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送过于频繁,请1分钟后尝试");
            }
        }
        int newSendCount = StringUtils.isNotBlank(sendCount) ? Integer.parseInt(sendCount) + 1 : 1;
        // 生成随机验证码
        String captcha = RandomStringUtils.randomNumeric(4);
        boolean result = sendCaptcha(redisKey, captcha);

        // 存redis
        hashOps.put("captcha", captcha);
        hashOps.put("lastSendTime", String.valueOf(System.currentTimeMillis()));
        hashOps.put("sendCount", String.valueOf(newSendCount));
        hashOps.expire(5, TimeUnit.MINUTES);   // 只在这里设 5 分钟（验证码本身有效期）

        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"发送邮件失败");
        }
        return captcha;
    }

    private boolean sendCaptcha(String hashKey, String captcha){
        // 发生随机验证码邮件
        boolean result = false;
        if (StringUtils.isNotBlank(hashKey)) {
            String toEmail = hashKey.split(":")[4];
            result = emailUtils.sendHtmlEmail(EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.getSubject(),
                    EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.set(captcha), toEmail);
        }
        return result;
    }

    /**
     * 根据账号获取用户信息
     * @param userAccount 用户账号
     * @return 用户信息
     */
    @Override
    public User getUserByAccount(String userAccount) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"暂无该用户");
        }
        return user;
    }

    /**
     * 用户忘记密码修改密码（根据邮箱验证码）
     * @param emailRequest  请求体封装类
     * @return  是否修改成功 true - 成功
     */
    @Override
    public Boolean resetUserPwd(UpdatePwdByEmailRequest emailRequest) {
        // 参数效验
        if (emailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入信息为空");
        }
        String userAccount = emailRequest.getUserAccount();     // 用户账号
        String email = emailRequest.getEmail();             // 获取用户的邮箱号
        String changePassword = emailRequest.getUserPassword();   // 用户修改的密码
        String emailCode = emailRequest.getEmailCode();     // 邮箱验证码
        // 判断是否有该用户
        User user = this.getUserByAccount(userAccount);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"暂无该用户");
        }
        // 从缓存中获取队该用户系统生成的邮箱验证码
        String redisKey = "login:" + userAccount + ":email:captcha:" + email;
        String systemEmailCode = (String) stringRedisTemplate.opsForHash().get(redisKey,"captcha");
        // 效验验证码是否正确
        if (systemEmailCode == null || !(systemEmailCode.equals(emailCode))) {
            // 验证码错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        // 验证码效验通过后开始修改用户的密码
        // 1、效验用户密码是否合法
        String valid = StringValidatorUtils.isValid(true, changePassword);
        // 2、用户从新修改的密码不合法
        if (valid != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,valid);
        }
        // 3、用户密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + changePassword).getBytes());
        // 4、修改用户密码到数据库
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", user.getId()).set("userPassword", newPassword);
        boolean result = this.update(updateWrapper);
        if (result) {
            // 修改成功删除缓存中的 redis（验证码）
            stringRedisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }

}




