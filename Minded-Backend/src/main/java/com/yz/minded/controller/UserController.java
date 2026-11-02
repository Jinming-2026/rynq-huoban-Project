package com.yz.minded.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yz.minded.common.BaseResponse;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.ResultUtils;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.request.UpdatePwdByEmailRequest;
import com.yz.minded.pojo.request.UpdatePwdRequest;
import com.yz.minded.pojo.request.UserLoginRequest;
import com.yz.minded.pojo.request.UserRegisterRequest;
import com.yz.minded.service.FollowRelationshipService;
import com.yz.minded.service.UserService;
import com.yz.minded.utils.StringValidatorUtils;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.yz.minded.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 * @author yz
 */
@ControllerAdvice
@RestController // 响应的数据类型都是 JSON
@RequestMapping("/user")
//说明接口文件
@Api(value = "用户接口", tags = "用户管理相关的接口")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @Resource
    private FollowRelationshipService followRelationshipService;

    /**
     * 用户注册
     * @param userRegisterRequest 前端传入参数
     * @return  新用户的 id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // @RequestBody 注解：将前端传过来的参数与 userRegisterRequest 对象做一个关联
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String username = userRegisterRequest.getUsername();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String email = userRegisterRequest.getEmail();
        if (StringUtils.isAnyBlank(username,userAccount, userPassword, checkPassword,email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容为空");
        }
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest  前端传入的用户账号、密码
     * @return 登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // @RequestBody 注解：将前端传过来的参数与 userRegisterRequest 对象做一个关联
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     * @param request  登录用户的 request
     * @return  1-注销成功  0-注销失败
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int logout = userService.userLogout(request);
        return ResultUtils.success(logout);
    }

    /**
     * 搜索用户
     * @return 用户列表
     */
    @GetMapping ("/search")
    public BaseResponse<List<User>> searchUsers(@RequestParam("username") String username,HttpServletRequest request){
        // 判断管理员权限
        if (userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"当前用户无权限");
        }
        // 查用户
        List<User> userList = userService.searchUsers(username);
        return ResultUtils.success(userList);
    }

    /**
     * 删除用户（逻辑删除）
     * @param id 用户id
     * @return 是否删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        // 判断管理员权限
        if (userService.isAdmin(request)) {
           throw new BusinessException(ErrorCode.NO_AUTH,"当前用户无权限");
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 用户修改信息
     * @param user  用户修改的信息
     * @return  1/0
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 判断参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        // 执行更新、判断用户权限
        Integer result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 修改密码
     * @param pwdRequest    用户传入密码信息
     * @param request   用户登录态
     * @return  修改是否成功
     */
    @PostMapping("/update/password")
    public BaseResponse<Integer> updateByPwd(@RequestBody UpdatePwdRequest pwdRequest, HttpServletRequest request) {
        // 判断参数是否为空
        if (pwdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        // 执行更新、判断用户权限
        Integer result = userService.updateByPwd(pwdRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 用户头像上传
     * @param file  头像
     * @return  r
     */
    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String Result = userService.uploadUserAvatarUrl(file, loginUser);
        return ResultUtils.success(Result);

    }
    // 捕获文件大小超限异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public BaseResponse<Object> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        // 可以在这里执行特定操作，比如回滚数据库等
        return ResultUtils.error(ErrorCode.PARAMS_ERROR,"文件大小超出限制！最大上传文件大小为5MB。");
    }


    /**
     * 获取用户的登录态
     * @param request 登录用户的session
     * @return  该用户的信息
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentObj = (User) userObj;
        if (currentObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"请先登录");
        }
        Long id = currentObj.getId();
        // 查询id用户信息
        User user = userService.getById(id);
        // 信息脱敏
        return ResultUtils.success(userService.getSafetyUser(user));
    }

    /**
     * 按标签查询用户（使用内存过滤）
     * @param tagList   标签列表
     * @return  用户列表
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagList) {
        // @RequestBody 中的 required 默认属性为 true，不能传空值，false 允许传入空值
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "标签列表为空");
        }
        return ResultUtils.success(userService.SearchUsersByTags(tagList));
    }

    /**
     * 获取所有用户
     * @param request   登录状态
     * @return  所有用户
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUser(long pageSize, long pageNum, HttpServletRequest request) {
        Page<User> userPage = userService.recommendUser(pageSize,pageNum,request);
        return ResultUtils.success(userPage);
    }

    /**
     * 获取最匹配的用户
     *
     * @param request request
     * @return UserPage
     */
    @GetMapping("/match")
    public BaseResponse<Page<User>> matchUsers(long num, long pageSize, long pageNum, HttpServletRequest request) {
        if (num <= 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 当前用户
        User loginUser = userService.getLoginUser(request);
        Page<User> userVoPage = userService.matchUsers(num,pageSize,pageNum, loginUser);
        return ResultUtils.success(userVoPage);
    }

    /**
     * 根据 id 获取用户消息
     * @param id    获取对象用户信息的 id
     */
    @GetMapping("/{id}")
    public BaseResponse<User> getUserById(@PathVariable(value = "id") long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User user = userService.getUserById(id);
        return ResultUtils.success(user);
    }

    @PostMapping("/captcha")
    public BaseResponse<String> getCaptcha(@RequestBody UpdatePwdByEmailRequest emailRequest) {
        // 获取用户输入的信息，进行比较数据库
        String userAccount = emailRequest.getUserAccount();
        String email = emailRequest.getEmail();
        // 效验邮箱
        boolean validEmail = StringValidatorUtils.isValidEmail(email);
        if (!validEmail) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱不合法");
        }
        //  根据账号获取用户信息
        User user = userService.getUserByAccount(userAccount);
        if (user == null || user.getEmail() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不存在或未绑定邮箱");
        }
        if (!user.getEmail().equals(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入的邮箱与账号绑定的不一致");
        }
        userService.sendMailCaptcha(emailRequest);
        // 注意：验证码只走邮件，不返回给前端（防抓包直接读到）
        return ResultUtils.success("验证码已发送至邮箱");

    }

    /**
     * 忘记密码，重置密码
     * @param emailRequest  emailRequest
     * @return  message
     */
    @PostMapping("/reset")
    public BaseResponse<String> resetUserPwd(@RequestBody UpdatePwdByEmailRequest emailRequest) {
        // 效验数据
        if (emailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入数据为空");
        }
        // 效验邮箱
        boolean validEmail = StringValidatorUtils.isValidEmail(emailRequest.getEmail());
        if (!validEmail) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱不合法");
        }
        Boolean result = userService.resetUserPwd(emailRequest);
        if (result) {
            return ResultUtils.success("成功");
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }


}
