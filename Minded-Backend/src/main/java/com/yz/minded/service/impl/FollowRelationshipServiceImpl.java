package com.yz.minded.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.ChatMessages;
import com.yz.minded.pojo.FollowRelationship;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.vo.FriendsVo;
import com.yz.minded.service.ChatMessagesService;
import com.yz.minded.service.FollowRelationshipService;
import com.yz.minded.mapper.FollowRelationshipMapper;
import com.yz.minded.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
* @author admin
* @description 针对表【follow_relationship(粉丝关注表)】的数据库操作Service实现
* @createDate 2024-10-22 20:11:08
*/
@Slf4j
@Service
public class FollowRelationshipServiceImpl extends ServiceImpl<FollowRelationshipMapper, FollowRelationship>
    implements FollowRelationshipService{

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChatMessagesService chatMessagesService;

    /**
     * 关注用户
     * @param id    需要关注用户的id
     * @param loginUser 登录用户
     * @return  返回关注信息
     */
    @Override
    public String doFollowUser(Long id, User loginUser) {
        if (id == null || id <= 0 || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (Objects.equals(id, loginUser.getId())) {          // 【修复③】Objects.equals
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能关注自己");
        }
        User followUser = userService.getById(id);
        if (followUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "被关注用户不存在");
        }
        if (isFollowerUser(id, loginUser) != -1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已关注该用户");
        }

        RLock lock = redissonClient.getLock("yz:follow_user:" + id);   // 按被关注者加锁，粒度更细
        boolean locked = false;
        try {
            // 【修复①】最多等 3 秒，不再 while(true) 自旋
            locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
            }
            FollowRelationship fr = new FollowRelationship();
            fr.setFollowerId(loginUser.getId());
            fr.setFollowedId(id);
            return this.save(fr) ? "关注成功" : "关注失败";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("关注失败", e);                            // 【修复②】类上有 @Slf4j 了
            return "关注失败";
        } finally {
            if (locked && lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    /**
     * 取关用户
     * @param id    取关用户的id
     * @param loginUser 当前登录用户
     * @return  取关信息
     */
    @Override
    public String unFollowUser(Long id, User loginUser) {
        // 参数效验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断取关用户是否存在
        User unFollowUser = userService.getById(id);
        if (unFollowUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否有关注关系
        long follwerUserId = isFollowerUser(id, loginUser);
        if (follwerUserId == -1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未关注该用户");
        }
        // 取关
        boolean removeById = this.removeById(follwerUserId);
        if (removeById) {
            return "取关成功";
        }
        return "取关失败";
    }

    /**
     * 获取粉丝数
     *
     * @param userId    用户id
     * @param loginUser 登录用户
     * @return 用户粉丝数
     */
    @Override
    public long getFansNum(Long userId, User loginUser) {
        // 参数效验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取粉丝数量
        QueryWrapper<FollowRelationship> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("followedId", userId);
        List<FollowRelationship> userFansNum = this.list(followQueryWrapper);
        return userFansNum.size();
    }

    /**
     * 获取关注数
     * @param userId    用户 id
     * @return  用户的关注数
     */
    @Override
    public long getFollowNum(Long userId) {
        // 参数效验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取关注数量
        QueryWrapper<FollowRelationship> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("followerId", userId);
        List<FollowRelationship> userFollowNum = this.list(followQueryWrapper);
        return userFollowNum.size();
    }

    /**
     * 获取用户的粉丝列表
     *
     * @param loginUser 当前登录用户
     * @return 用户的粉丝
     */
    @Override
    public List<User> getFansList(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = loginUser.getId();
        // 获取粉丝表中的信息
        QueryWrapper<FollowRelationship> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("followedId", userId);
        List<FollowRelationship> followRelationshipList = this.list(followQueryWrapper);
        if (followRelationshipList.isEmpty()) {
            return new ArrayList<>();
        }
        // 获取粉丝 id 列表
        List<Long> fansIdList = followRelationshipList.stream()
                .map(FollowRelationship::getFollowerId)
                .toList();
        // 根据粉丝 id 列表获取粉丝用户列表集合
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", fansIdList);
        List<User> fansList = userService.list(userQueryWrapper);
        // 用户信息脱敏
        List<User> safetyFans = new ArrayList<>();
        for (User user: fansList) {
            User safetyUser = userService.getSafetyUser(user);
            // 封装返回信息
            safetyFans.add(safetyUser);
        }
        return safetyFans;
    }

    /**
     * 判断当前用户是否关注该用户
     * @param id    被关注者 id
     * @param loginUser 当前登录用户
     * @return  关注信息的 id
     */
    @Override
    public long isFollowerUser(Long id, User loginUser) {
        QueryWrapper<FollowRelationship> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("followerId", loginUser.getId());
        followQueryWrapper.eq("followedId", id);
        List<FollowRelationship> followRelationshipList = this.list(followQueryWrapper);
        // 有数据则为用户关注的那一条（已关注）
        // 返回关注信息那一条的id
        if (followRelationshipList != null && !followRelationshipList.isEmpty()) {
            return followRelationshipList.get(0).getId();
        }
        // 没用关注信息
        return -1;
    }

    /**
     * 获取用户关注列表
     * @param loginUser 当前登录用户
     * @return  用户关注列表
     */
    @Override
    public List<User> getFollowerList(User loginUser) {
//        1. 参数效验
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = loginUser.getId();
//        2. 在用户关系表中判断查询是否有 关注 关系
        QueryWrapper<FollowRelationship> followRelationshipQueryWrapper = new QueryWrapper<>();
        followRelationshipQueryWrapper.eq("followerId", userId);
        List<FollowRelationship> followRelationshipList = this.list(followRelationshipQueryWrapper);
//        3. 若有，获取关注用户的 id 列表；若无，则返回空列表
        if (followRelationshipList == null || followRelationshipList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> followIdList = followRelationshipList.stream()
                .map(FollowRelationship::getFollowedId)
                .toList();
//        4. 根据关注用户的 id 去查询对应的用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", followIdList);
        List<User> followUserList = userService.list(userQueryWrapper);
//        5. 用户信息脱敏
        List<User> safetyFollowers = new ArrayList<>();
        for (User user: followUserList) {
            User safetyUser = userService.getSafetyUser(user);
            safetyFollowers.add(safetyUser);
        }
//        6. 封装返回类
        return safetyFollowers;
    }

    /**
     * 获取用户的好友
     *
     * @param loginUser 登录用户信息
     * @return 用户的 关注 和 粉丝
     */
    @Override
    public List<FriendsVo> getFriendsList(User loginUser) {
        // 效验参数
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long loginUserId = loginUser.getId();
        // 获取用户的关注用户和粉丝
        QueryWrapper<FollowRelationship> relationshipQueryWrapper = new QueryWrapper<>();
        relationshipQueryWrapper
                .eq("followerId", loginUserId)  // 关注
                .or()
                .eq("followedId", loginUserId); // 粉丝
        List<FollowRelationship> followRelationshipList = this.list(relationshipQueryWrapper);
        if (followRelationshipList == null || followRelationshipList.isEmpty()) {
            // 没有数据
            return new ArrayList<>();
        }
        // 获取用户所有粉丝 id
        List<Long> fansIdList = followRelationshipList.stream()
                .map(FollowRelationship::getFollowedId)
                .toList();
        // 获取用户所有关注 id
        List<Long> followerIdList = followRelationshipList.stream()
                .map(FollowRelationship::getFollowerId)
                .toList();
        // 构造条件获取关注/粉丝用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (!fansIdList.isEmpty() && !followerIdList.isEmpty()) {
            userQueryWrapper.in("id", fansIdList).or().in("id", followerIdList);
        } else if (fansIdList.isEmpty()) {
            userQueryWrapper.in("id", followerIdList);
        } else {
            userQueryWrapper.in("id", fansIdList);
        }
        List<User> friendsList = userService.list(userQueryWrapper);
        // 排除自己
        friendsList = friendsList.stream()
                .filter(user -> !Objects.equals(user.getId(), loginUserId))
                .toList();
        // 构造数据
        List<FriendsVo> friendsVoList = new ArrayList<>();
        friendsList.forEach(user -> {
            // 设置好友信息
            FriendsVo friendsVo = new FriendsVo();
            friendsVo.setId(user.getId());
            friendsVo.setName(user.getUsername());
            friendsVo.setAvatar(user.getAvatarUrl());
            // 设置用户是否有最新消息、用户之间最新的一条聊天信息、最新一条消息的时间
            // 获取用户之间最新的一条消息
            ChatMessages oneNewMessage = chatMessagesService.getOneNewMessage(loginUser, user);
            // 用户之间有未读消息、设置用户之间最新一条消息
            if (oneNewMessage != null
                    && Objects.equals(oneNewMessage.getReceiverId(), loginUserId)   // 注意 receiverId 类型见第 8 步
                    && (oneNewMessage.getReadStatus() == null || oneNewMessage.getReadStatus() == 1)) {
                friendsVo.setHasUnRead(true);
            }
            if (oneNewMessage != null) {
                friendsVo.setOneNewMessage(oneNewMessage.getMessage());
                friendsVo.setTimestamp(oneNewMessage.getTimestamp());
            }
            friendsVoList.add(friendsVo);
        });
        // 按照消息的时间进行排序
        List<FriendsVo> resultFriendsVoList;
        resultFriendsVoList = friendsVoList.stream()
                .sorted((c1, c2) -> {
                    if (c2.getTimestamp() == null || c1.getTimestamp() == null) return 0;
                    long time = c1.getTimestamp().getTime() - c2.getTimestamp().getTime();
                    if (time == 0) return 0;
                    if (time < 0) return 1;
                    return -1;
                }).toList();
        return resultFriendsVoList;
    }

}




