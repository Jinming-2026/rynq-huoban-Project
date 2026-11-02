package com.yz.minded.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 粉丝关注表
 * @TableName follow_relationship
 */
@TableName(value ="follow_relationship")
@Data
public class FollowRelationship implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关注者 id
     */
    private Long followerId;
    // followerId -->(关注了) followedId
    /**
     * 被关注者 id
     */
    private Long followedId;

    /**
     * 关注时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**
     * 0 - 未删除，1 - 已删除
     */
    @TableLogic
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}