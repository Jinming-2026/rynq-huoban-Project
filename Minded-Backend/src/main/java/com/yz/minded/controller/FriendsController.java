package com.yz.minded.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yz.minded.common.BaseResponse;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.ResultUtils;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.vo.FriendsVo;
import com.yz.minded.service.FollowRelationshipService;
import com.yz.minded.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户关系接口
 */
@RestController
@Controller
@RequestMapping("/friends")
//说明接口文件
@Api(value = "用户关系接口", tags = "用户关系管理相关的接口")
public class FriendsController {

    @Resource
    private UserService userService;

    @Resource
    private FollowRelationshipService followRelationshipService;

    /**
     * 关注用户
     * @param id    关注用户的id
     * @param request   request
     * @return  关注信息
     */
    @PostMapping("/dofollow/{id}")
    public BaseResponse<String> followUser(@PathVariable(value = "id") long id, HttpServletRequest request){
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String followResult = followRelationshipService.doFollowUser(id, loginUser);
        return ResultUtils.success(followResult);
    }

    /**
     * 取关
     * @param id    取关用户 id
     * @param request   request
     * @return  取关信息
     */
    @PostMapping("/unfollow/{id}")
    public BaseResponse<String> unfollowUser(@PathVariable(value = "id") long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String unFollowResult = followRelationshipService.unFollowUser(id, loginUser);
        return ResultUtils.success(unFollowResult);
    }

    // 获取用户粉丝数量
    @GetMapping("/fansNum")
    public BaseResponse<Long> getFansNum(long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long fansNum = followRelationshipService.getFansNum(id, loginUser);
        return ResultUtils.success(fansNum);
    }
    // 获取用户关注数
    @GetMapping("/followNum")
    public BaseResponse<Long> getFollowNum(long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long followNum = followRelationshipService.getFollowNum(id);
        return ResultUtils.success(followNum);
    }

    // 获取用户粉丝列表
    @GetMapping("/myFans")
    public BaseResponse<Page<User>> getFansPage(long pageNum, long pageSize, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> fansList = followRelationshipService.getFansList(loginUser);
        // 设置分页（调用 userService 里面的分页方法
        Page<User> fansPage = userService.doPage(fansList, pageNum, pageSize);
        return ResultUtils.success(fansPage);
    }

    // 判断登录用户是否关注该用户
    @GetMapping("/isFollow")
    public BaseResponse<Boolean> isFollow(Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long followerUser = followRelationshipService.isFollowerUser(id, loginUser);
        if (followerUser != -1) {
            return ResultUtils.success(true);
        }
        return ResultUtils.success(false);
    }

    // 获取用户关注用户列表
    @GetMapping("/myFollower")
    public BaseResponse<Page<User>> myFollowerPage(long pageNum, long pageSize, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> followerList = followRelationshipService.getFollowerList(loginUser);
        // 调用 userService 中实现的分页方法
        Page<User> followerPage = userService.doPage(followerList, pageNum, pageSize);
        return ResultUtils.success(followerPage);
    }

    // 获取用户的所有好友（关注 + 粉丝）
    @GetMapping("/all")
    public BaseResponse<List<FriendsVo>> getAllFriendsList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<FriendsVo> friendsList = followRelationshipService.getFriendsList(loginUser);
        return ResultUtils.success(friendsList);
    }

}
