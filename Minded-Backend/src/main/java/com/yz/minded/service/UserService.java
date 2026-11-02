package com.yz.minded.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.request.UpdatePwdByEmailRequest;
import com.yz.minded.pojo.request.UpdatePwdRequest;
import com.yz.minded.pojo.request.UserRegisterRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *  用户服务
* @author yz
* @description 针对表【user_center(用户中心表)】的数据库操作Service
* @createDate 2024-08-31 14:34:35
*/
public interface UserService extends IService<User> {

    /**
     * 注册用户
     * @param userRegisterRequest@return 新注册用户的 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param request  a
     * @return  用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户修改信息
     * @param user  修改内容
     * @param loginUser 当前用户的信息
     * @return  1/0
     */
    Integer updateUser(User user, User loginUser);

    /**
     * 用户脱敏
     * @param originUser 用户未脱敏信息
     * @return 脱敏后的信息
     */
    User getSafetyUser(User originUser);

    /**
     * 请求用户注销
     * @param request request
     * @return 1/0
     */
    int userLogout(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @param request 登录用户的session
     * @return true：管理员 false：普通用户
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @param loginUser 登录的用户
     * @return  true：管理员 false：普通用户
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取当前用户信息
     * @param request   当前用户的 Cookie
     * @return  当前用户的信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 搜索用户
     * @param username 用户账号
     * @return  用户信息
     */
    List<User> searchUsers(String username);

    /**
     * 根据标签查询用户（SQL版）、要包含所有标签才能被搜索出来
     * @param tagList 标签列表
     * @return 用户列表
     */
    List<User> SQLSearchUsersByTags(List<String> tagList);

    /**
     * 根据标签查询用户（内存过滤）、只要含有其中一个标签就能被搜索出来
     * @param tagList   标签列表
     * @return  用户列表
     */
    List<User> SearchUsersByTags(List<String> tagList);

    /**
     * 主页用户展示
     * @param pageSize 每一页展示的用户数量
     * @param pageNum   当前的页码
     * @param request   当前登录用户的 request
     * @return  分页查询出来的用户列表
     */
    Page<User> recommendUser(long pageSize, long pageNum, HttpServletRequest request);

    /**
     * 更改用户密码
     * @param pwdRequest    传入的密码参数
     * @param loginUser 用户信息
     * @return  修改成功
     */
    Integer updateByPwd(UpdatePwdRequest pwdRequest, User loginUser);

    /**
     * 用户上传头像
     * @param file  头像
     * @return r
     */
    String uploadUserAvatarUrl(MultipartFile file, User loginUser);

    /**
     * 获取最推荐用户
     *
     * @param num       推荐数量
     * @param pageSize  s
     * @param pageNum   n
     * @param loginUser 登录用户
     * @return 推荐的用户
     */
    Page<User> matchUsers(long num, long pageSize, long pageNum, User loginUser);

    /**
     * 设置分页
     * @param userList  分页的列表
     * @param pageNum   当前页数
     * @param pageSize  页面的大小
     * @return  分页后的 page
     */
    Page<User> doPage(List<User> userList, long pageNum, long pageSize);

    /**
     * 获取对象用户的信息
     * @param id    对象用户的id
     * @return  对象用户信息
     */
    User getUserById(long id);

    /**
     * 发送邮箱验证码
     *
     * @param emailRequest @return 是否成功
     */
    String sendMailCaptcha(UpdatePwdByEmailRequest emailRequest);

    /**
     * 更具账号获取用户信息
     * @param userAccount 用户账号
     * @return  用户信息
     */
    User getUserByAccount(String userAccount);

    /**
     * 用户忘记密码修改密码
     * @param emailRequest  请求体封装类
     * @return  是否修改成功
     */
    Boolean resetUserPwd(UpdatePwdByEmailRequest emailRequest);
}
