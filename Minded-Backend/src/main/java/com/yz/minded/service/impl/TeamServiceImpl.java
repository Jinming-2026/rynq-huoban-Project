package com.yz.minded.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.pojo.UserTeam;
import com.yz.minded.pojo.dto.TeamQuery;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.Team;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.enums.TeamStatusEnum;
import com.yz.minded.pojo.request.TeamJoinRequest;
import com.yz.minded.pojo.request.TeamQuitRequest;
import com.yz.minded.pojo.request.TeamUpdateRequest;
import com.yz.minded.pojo.vo.TeamUserVo;
import com.yz.minded.pojo.vo.UserVo;
import com.yz.minded.service.TeamService;
import com.yz.minded.mapper.TeamMapper;
import com.yz.minded.service.UserService;
import com.yz.minded.service.UserTeamService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author yz
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2024-10-06 22:08:52
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    /**
     * 添加队伍的实现类
     * @param team 队伍信息
     * @return 添加成功或失败的信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   //开启事务
    public long addTeam(Team team, User loginUser) {
       // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
       // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        // 3. 效验信息：
        //    a. 队伍人数 > 1 && <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);   // 如果为空 默认值为0
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //    b. 队伍标题 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不满足要求");
        }
        //    c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //    d. status 是否公开（int）不传默认为 0 （公开）
        Integer statusBox = team.getStatus();
        int status = (statusBox == null) ? 0 : statusBox;
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //    e. 如果是加密状态，一定要有密码，且密码 <= 32
        String teamPwd = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum) ) {
            if (StringUtils.isBlank(teamPwd) || teamPwd.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
        //    f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "设置的超时时间必须大于当前时间");
        }
        //    g. 校验用户最多创建 5 个队伍
        // TODO 有 bug，可能同时创建 >= 100 个队伍，疯狂点击
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(queryWrapper);
        // 判断当前用户是否为管理员
        boolean isAdmin = userService.isAdmin(loginUser);
        if (hasTeamNum >= 5 && !isAdmin) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        if (!result || team.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        // 5. 插入 用户 => 队伍关系 到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }

        return team.getId();
    }


    /**
     * 搜索队伍
     *
     * @param teamQuery 队伍查询封装类
     * @param loginUser 登录用户信息
     * @return 队伍列表
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, User loginUser) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 如果为空，搜索所有队伍（组合条件查询）
        if (teamQuery != null) {
            // 根据队伍 id 查询
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            // 根据关键字查询（同时对队伍名和队伍描述查询）
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据队伍名查询
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            // 根据队伍描述查询
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            // 查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.lt("maxNum", maxNum);
            }
            // 根据创建人来查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            if (status != null && status > -1) {
                queryWrapper.eq("status", status);
            }
        }
        // 判断是否展示过期的队伍
        if (teamQuery == null) {
            return new ArrayList<>();
        }
        if (Boolean.TRUE.equals(teamQuery.getTimeOut())) {   // 防止 Boolean 为 null
            queryWrapper.and(qw -> qw.lt("expireTime", new Date()).or().isNull("expireTime"));
        } else {
            queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        }
        // and expireTime is null or expireTime > now()
        queryWrapper.orderByDesc("updateTime");
        List<Team> teamList = this.list(queryWrapper);
        // 查询队伍为空则直接返回空列表
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 获取队伍列表 id
        List<Long> teamIdList = teamList.stream().map(Team::getId).toList();

        return getTeamUserVos(loginUser, teamList, teamIdList);
    }
    // 查询队伍创建人信息、设置队伍加入人数、判断当前登录用户是否加入队伍
    private List<TeamUserVo> getTeamUserVos(User loginUser, List<Team> teamList, List<Long> teamIdList) {
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) continue;

            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);

            if (user != null) {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);   // 建议改为 userService.getSafetyUser(user) 再 copy
                teamUserVo.setCreatUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        // 循环外统一处理
        if (!teamUserVoList.isEmpty()) {
            this.hasJoinTeam(teamUserVoList, loginUser.getId());
            this.setTeamJoinNum(teamUserVoList, teamIdList);
        }
        return teamUserVoList;
    }

    /**
     * 判断用户是否参加队伍
     * @param teamUserVoList    符合条件的队伍列表
     * @param userId    用户 id
     * @return  增加 hasJoinTeam 的 队伍表
     */
    @Override
    public List<TeamUserVo> hasJoinTeam(List<TeamUserVo> teamUserVoList, long userId){
        // 获取队伍 id 列表
        List<Long> teamIdList = teamUserVoList.stream()
                .map(TeamUserVo::getId)
                .toList();
        // 判断当前用户是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        userTeamQueryWrapper.in("teamId",teamIdList);
        // 当前用户加入的队伍集合
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        Set<Long> hasJoinTeamIdSet = userTeamList.stream()
                .map(UserTeam::getTeamId)
                .collect(Collectors.toSet());

        teamUserVoList.forEach(team -> {
            boolean result = hasJoinTeamIdSet.contains(team.getId());
            team.setHasJoin(result);
        });
        return teamUserVoList;
    }

    /**
     * 设置队伍已加入的人数
     * @param teamUserVoList    符合要求的队伍列表
     * @param teamIdList    队伍 id 列表
     */
    public void setTeamJoinNum(List<TeamUserVo> teamUserVoList,List<Long> teamIdList) {
        if (!((teamIdList != null && !teamIdList.isEmpty()) && (teamUserVoList != null && !teamUserVoList.isEmpty()))) {
            return;
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper); // 当前用户创建的队伍集合
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId)); // 根据队伍id分组
        teamUserVoList.forEach(team -> {
            // 设置每个队伍的人数
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
    }

    /**
     * 修改队伍信息
     * @param teamUpdateRequest 需要修改的信息
     * @param loginUser 当前登录用户
     * @return 是否修改成功
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"不存在该队伍");
        }
        // 只有队伍创建者和管理员才能修改队伍信息
        if (loginUser.getId() != oldTeam.getUserId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 获取队伍需要修改成的状态
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        // 若改为加密状态则必须要有密码
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密队伍必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.updateById(updateTeam);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest 加入队伍请求信息
     * @param loginUser     当前登录的用户
     * @return 是否加入成功
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        Integer status = team.getStatus();
        if (status == null) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍状态异常");
//        1. 禁止加入私有的队伍
//        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"禁止加入私有队伍");
        }
//        2. 如果加入队伍是加密的，必须密码匹配
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !team.getPassword().equals(password)) {
                throw new BusinessException(ErrorCode.NULL_ERROR,"密码错误");
            }
        }

//        3. 用户最多加入 5 个队伍
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入 5 个队伍");
        }

//        4. 不能重复已加入的队伍（幂等性）
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long hasUserJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (hasUserJoinNum > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户已加入该队伍");
        }

//        5. 只能加入未满、未过期、其他人创建的队伍
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已过期");
        }
        // 已加入队伍的人数
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        long teamJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (teamJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已满");
        }

//        6. 新增队伍-用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }

    /**
     * 退出队伍
     * @param teamQuitRequest   对出队伍id
     * @param loginUser 退出的用户信息
     * @return  是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   // 事务注解
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
//        1. 请求参数：队伍 Id
//              a. 效验请求参数
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        2. 校验队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
//        3. 校验是否已加入队伍
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        // 判断 userId 和 teamId 之间的关系性
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }
//        4. 如果队伍只剩一人，则队伍解散
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        // 加入队伍的人数
        long joinTeamNum = userTeamService.count(userTeamQueryWrapper);
        if (joinTeamNum == 1) {
            // 删除队伍和所有加入队伍的关系
            this.removeById(teamId);    // 删除当前队伍的 id
            // 删除用户队伍关系
            userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
            return userTeamService.remove(userTeamQueryWrapper);
        } else {
//        5. 退出队伍
//              a. 如果是队长退出队伍，权限转移到第二早加入队伍的用户 ------ 先来后到
//              b. 非队长退出，直接退出队伍
            // 队伍至少还剩两人，判断是否为队长
            if (team.getUserId() != null && userId == team.getUserId()) {                // 把队长转给最早加入的用户
                // 1、查询加入队伍的所有用户
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 将队长位置转给 nextTeamLeaderId
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队长失败");
                }
            }
            // 移除当前用户 用户队伍 之间的关系
            return userTeamService.remove(queryWrapper);
        }
    }

    /**
     * 根据 id 获取队伍信息
     * @param teamId 队伍id
     * @return  队伍信息
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        return team;
    }

    /**
     * 删除（解散）队伍
     *
     * @param teamId    队伍 id
     * @param loginUser     登录用户信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   // 事务注解
    public boolean disbandTeam(long teamId, User loginUser) {
//        1. 校验请求参数
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        2. 校验队伍是否存在（获取队伍信息）
        Team team = this.getTeamById(teamId);
//        3. 校验是否为队长
        if (team.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH,"不是该队伍队长");
        }
//        4. 移除所有加入队伍的关联信息（事务）
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean removerUserTeamResult = userTeamService.remove(userTeamQueryWrapper);
        if (!removerUserTeamResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
//        5. 删除队伍（事务）
        return this.removeById(teamId);
    }

    /**
     * 获取加入该队伍的用户
     * @param teamId 队伍 id
     * @return 加入队伍的用户列表
     */
    @Override
    public List<UserVo> teamMemberList(Long teamId) {
//        1. 判断请求参数是否错误
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        2. 查询用户队伍信息表，得到参加队伍的用户 id
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        // 得到加入队伍的用户 id 集合
        List<Long> memberIds = userTeamList.stream().map(UserTeam::getUserId).toList();
//        3. 根据得到的用户 id 去查询用户信息（User）
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", memberIds);
        List<User> memverList = userService.list(userQueryWrapper);
//        4. 将查询到的信息进行封装成 UserVo 返回给前端
        List<UserVo> memberVoList = new ArrayList<>();
// 方案 A：先脱敏再 copy（推荐）
        memverList.forEach(user -> {
            User safe = userService.getSafetyUser(user);
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(safe, userVo);
            memberVoList.add(userVo);
        });
        return memberVoList;
    }

    /**
     * 查询用户加入队伍集合
     * @param teamQuery teamQuery
     * @param loginUser loginUser
     * @return 用户加入队伍集合
     */
    @Override
    public List<TeamUserVo> joinTeamList(TeamQuery teamQuery, User loginUser) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据登录用户 id 获取加入的队伍集合
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        if (userTeamList == null || userTeamList.isEmpty()) {
            // 用户没有加入的队伍
            return new ArrayList<>();
        }
        // 获取所有的队伍 id 集合
        List<Long> teamIdList = userTeamList.stream().map(UserTeam::getTeamId).toList();
        // 查询队伍信息，过滤掉自己创建的队伍
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.in("id", teamIdList);
        teamQueryWrapper.ne("userId",userId);
        // 判断是否展示过期的队伍
        if (teamQuery.getTimeOut()) { // 展示
            teamQueryWrapper.and(qw -> qw.lt("expireTime", new Date()).or().isNull("expireTime"));
        } else {
            teamQueryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        }

        List<Team> teamList = this.list(teamQueryWrapper);
        // 最终返回的列表
        return getTeamUserVos(loginUser, teamList, teamIdList);
    }

}




