package com.yz.minded.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息返回实体类
 */
@Data
public class ChatMessagesVo implements Serializable {
    @Serial
    private static final long serialVersionUID = -4665241817665684056L;

    /**
     * 发送者id
     */
    private long senderId;

    /**
     * 接收者id
     */
    private String receiverId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date timestamp;

    /**
     * 头像url
     */
    private String avatar;

    /**
     * 0-未读，1-已读
     */
    private Integer readStatus;
}
