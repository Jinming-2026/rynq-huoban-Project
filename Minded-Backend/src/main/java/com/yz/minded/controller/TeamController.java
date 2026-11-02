package com.yz.minded.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yz.minded.common.BaseResponse;
import com.yz.minded.common.DeleteRequest;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.ResultUtils;
import com.yz.minded.pojo.request.TeamAddRequest;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.dto.TeamQuery;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.Team;
import com.yz.minded.pojo.request.TeamJoinRequest;
import com.yz.minded.pojo.request.TeamQuitRequest;
import com.yz.minded.pojo.request.TeamUpdateRequest;
import com.yz.minded.pojo.vo.TeamUserVo;
import com.yz.minded.pojo.vo.UserVo;
import com.yz.minded.service.TeamService;
import com.yz.minded.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 队伍接口
 * @author yz
 */
@RestController // 响应的数据类型都是 JSON
@RequestMapping("/team")
//说明接口文件
@Api(value = "队伍接口", tags = "队伍管理相关的接口")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    // 添加队伍
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空，添加队伍失败");
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(team,teamAddRequest);
        long result = teamService.addTeam(team, loginUser);
        return ResultUtils.success(result);
    }

    // 删除队伍
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    // 修改队伍信息
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空，修改信息失败");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    // 根据 id 查询队伍
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍信息为空");
        }
        return ResultUtils.success(team);
    }

    /**
     * 搜索队伍
     * @param teamQuery 队伍查询封装类
     * @return  队伍列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<TeamUserVo>> listTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,loginUser);

        // 设置分页
        Page<TeamUserVo> teamUserVoPage = getPage(teamQuery, teamList);
        return ResultUtils.success(teamUserVoPage);
    }

    /**
     * 设置分页
     * @param teamQuery 查询队伍的信息
     * @param teamList  查询到的队伍列表
     * @return  分页后的队伍Page
     */
    private static Page<TeamUserVo> getPage(TeamQuery teamQuery, List<TeamUserVo> teamList) {
        int total = teamList.size();
        int pageNum = teamQuery.getPageNum();
        int pageSize = teamQuery.getPageSize();
        if (pageNum * pageSize >= total) {
            teamList = teamList.subList(pageSize * (pageNum - 1), total);
        } else {
            teamList = teamList.subList(pageSize*(pageNum - 1), pageNum*pageSize);
        }
        Page<TeamUserVo> teamUserVoPage = new Page<>();
        teamUserVoPage.setTotal(total);
        teamUserVoPage.setPages(pageNum);
        teamUserVoPage.setSize(pageSize);
        teamUserVoPage.setRecords(teamList);
        teamUserVoPage.setCurrent(pageNum);
        return teamUserVoPage;
    }

    // TODO 关联查询已加入队伍的用户信息（可能会很耗费性能，建议用自己写SQL的方式实现）

    // 分页查询队伍列表
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> getListTeamByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamQuery);
            // 将 teamQuery 映射给 team
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);
    }

    // 加入队伍
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    // 退出队伍
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest,HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }

    // 队长解散队伍
    @PostMapping("/disband")
    public BaseResponse<Boolean> disbandTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long teamId = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.disbandTeam(teamId,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解散失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询用户所创建的队伍
     * @param teamQuery 查询信息
     * @param request   用户登录态
     * @return  查询所创建的队伍 Page
     */
    @GetMapping("/list/create")
    public BaseResponse<Page<TeamUserVo>> myCreateTeam(TeamQuery teamQuery,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = loginUser.getId();
        teamQuery.setUserId(userId);    // 将用户 id 作为查询条件（创建人）
        List<TeamUserVo> myCreateTeamList = teamService.listTeams(teamQuery,loginUser);
        // 设置分页
        Page<TeamUserVo> myCreateTeamPage = getPage(teamQuery, myCreateTeamList);
        return ResultUtils.success(myCreateTeamPage);

    }

    /**
     * 获取加入该队伍的用户
     * @param teamId    队伍 id
     * @return  加入队伍的用户列表
     */
    @GetMapping("/member")
    public BaseResponse<List<UserVo>> teamMember(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<UserVo> userVoList = teamService.teamMemberList(teamId);
        return ResultUtils.success(userVoList);
    }

    /**
     * 获取创建已过期的队伍
     * @param teamQuery 查询条件
     * @param request   request
     * @return  用户创建已过期的队伍 Page
     */
    @GetMapping("/list/timeout")
    public BaseResponse<Page<TeamUserVo>> timeOutTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVo> teamUserVoList = teamService.listTeams(teamQuery, loginUser);
        // 设置分页
        Page<TeamUserVo> timeOutTeamPage = getPage(teamQuery, teamUserVoList);
        return ResultUtils.success(timeOutTeamPage);
    }

    // 查询用户加入的队伍
    @GetMapping("/list/join")
    public BaseResponse<Page<TeamUserVo>> joinTeamPage(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVo> joinTeamList = teamService.joinTeamList(teamQuery,loginUser);
        Page<TeamUserVo> teamUserVoPage = getPage(teamQuery, joinTeamList);
        return ResultUtils.success(teamUserVoPage);
    }

}
