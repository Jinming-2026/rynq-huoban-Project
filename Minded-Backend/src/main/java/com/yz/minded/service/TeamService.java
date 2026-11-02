package com.yz.minded.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yz.minded.pojo.dto.TeamQuery;
import com.yz.minded.pojo.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.request.TeamJoinRequest;
import com.yz.minded.pojo.request.TeamQuitRequest;
import com.yz.minded.pojo.request.TeamUpdateRequest;
import com.yz.minded.pojo.vo.TeamUserVo;
import com.yz.minded.pojo.vo.UserVo;

import java.util.List;

/**
* @author admin
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-10-06 22:08:52
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     *
     * @param team      队伍信息
     * @param loginUser 当前登录的用户
     * @return 是否添加成功
     */
    long addTeam(Team team, User loginUser);


    /**
     * 搜索队伍
     *
     * @param teamQuery 队伍查询封装类
     * @param loginUser 登录用户信息
     * @return 队伍列表
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, User loginUser);

    /**
     * 修改队伍信息
     *
     * @param teamUpdateRequest 需要修改的信息
     * @param loginUser 当前登录用户
     * @return 是否修改成功
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍请求信息
     * @param loginUser     当前登录的用户
     * @return 是否成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest   对出队伍id
     * @param loginUser 退出的用户信息
     * @return  是否成功
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除（解散）队伍
     *
     * @param teamId    队伍 id
     * @param loginUser     登录用户信息
     * @return 是否成功
     */
    boolean disbandTeam(long teamId, User loginUser);

    List<TeamUserVo> hasJoinTeam(List<TeamUserVo> teamUserVoList, long userId);

    /**
     * 获取加入该队伍的用户
     * @param teamId 队伍 id
     * @return  加入队伍的用户列表
     */
    List<UserVo> teamMemberList(Long teamId);

    /**
     * 用户加入的队伍集合
     * @param teamQuery teamQuery
     * @param loginUser loginUser
     * @return  用户加入队伍集合
     */
    List<TeamUserVo> joinTeamList(TeamQuery teamQuery, User loginUser);
}
