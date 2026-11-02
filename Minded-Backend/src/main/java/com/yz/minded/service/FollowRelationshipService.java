package com.yz.minded.service;

import com.yz.minded.pojo.FollowRelationship;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.vo.FriendsVo;

import java.util.List;

/**
* @author admin
* @description 针对表【follow_relationship(粉丝关注表)】的数据库操作Service
* @createDate 2024-10-22 20:11:08
*/
public interface FollowRelationshipService extends IService<FollowRelationship> {
    /**
     * 关注用户
     * @param id    关注用户的id
     * @param loginUser 登录用户
     * @return  关注信息
     */
    String doFollowUser(Long id, User loginUser);

    /**
     * 取关用户
     * @param id    取关用户的id
     * @param loginUser 当前登录用户
     * @return  取关信息
     */
    String unFollowUser(Long id, User loginUser);

    /**
     * 获取粉丝数
     *
     * @param userId    用户id
     * @param loginUser 登录用户
     * @return 用户粉丝数
     */
    long getFansNum(Long userId, User loginUser);

    /**
     * 获取关注数
     * @param userId    用户 id
     * @return  用户的关注数
     */
    long getFollowNum(Long userId);

    /**
     * 获取用户粉丝列表
     *
     * @param loginUser 当前登录用户
     * @return 用户的粉丝
     */
    List<User> getFansList(User loginUser);

    /**
     * 判断当前用户是否关注该用户
     * @param id    被关注者 id
     * @param loginUser 登录用户
     * @return  关注信息的列 id
     */
    long isFollowerUser(Long id, User loginUser);

    /**
     * 获取登录用户关注与列表
     * @param loginUser 当前登录用户
     * @return  用户关注列表
     */
    List<User> getFollowerList(User loginUser);

    /**
     * 获取登录用户的好友（关注 + 粉丝）
     *
     * @param loginUser 登录用户信息
     * @return 登录用户的 关注 和 粉丝
     */
    List<FriendsVo> getFriendsList(User loginUser);

}
