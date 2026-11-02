package com.yz.minded.pojo.request;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 队伍表
 * @TableName team
 */
@TableName(value ="team")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamAddRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5140869335245683288L;
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date expireTime;

    /**
     * 用户Id
     */
    private Long userId;


    /**
     * 0-公开、1-加密
     */
    private Integer status = 0;

    /**
     * 密码
     */
    private String password;


}