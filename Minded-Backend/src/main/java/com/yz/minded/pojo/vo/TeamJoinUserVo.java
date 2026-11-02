package com.yz.minded.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TeamJoinUserVo implements Serializable {
    @Serial
    private static final long serialVersionUID = -9058282866968740096L;

    /**
     * 队伍id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 所有加入队伍的用户（不显示已退出用户）
     * 一个队伍拥有多个用户
     */
    private List<UserVo> joinUser;

}
