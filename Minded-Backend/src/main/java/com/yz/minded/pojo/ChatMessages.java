package com.yz.minded.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户私聊消息表
 * @TableName chat_messages
 */
@TableName(value ="chat_messages")
@Data
public class ChatMessages implements Serializable {
    /**
     * 
     */
    @TableId(value = "chat_id",type = IdType.AUTO)
    private Long chatId;

    /**
     * 发送者 id
     */
    @TableField(value = "sender_id")
    private Long senderId;

    /**
     * 接收者 id
     */
    @TableField(value = "receiver_id")
    private Long receiverId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date timestamp;

    @TableField(value = "read_status")
    private Integer readStatus;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}