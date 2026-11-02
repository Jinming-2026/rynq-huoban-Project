package com.yz.minded.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 队伍和用户信息封装类（脱敏）
 * @author yz
 */
@Data
public class TeamUserVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 7995497671721029335L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date expireTime;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 队伍头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;

    /**
     * 0-公开、1-加密
     */
    private Integer status;

    /**
     * 创建人信息
     */
    UserVo creatUser;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;

    /**
     * 队伍人数
     */
    private Integer hasJoinNum;

}
