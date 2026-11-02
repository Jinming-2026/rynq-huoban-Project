package com.yz.minded.WebSocket.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息接收实体类
 */
@Data
public class ResultMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 2828050911804728845L;
    /**
     * 发送者id
     */
    private long senderId;

    /**
     * 接收者id
     */
    private long receiverId;

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
     * 头像
     */
    private String avatar;

    /**
     * 0-未读，1-已读
     */
    private Integer readStatus;
}
