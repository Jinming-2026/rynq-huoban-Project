package com.yz.minded.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 好友列表返回体封装类
 * @author yz
 */
@Data
public class FriendsVo implements Serializable {
    @Serial
    private static final long serialVersionUID = -7191414338258832364L;
    /**
     * 好友id
     */
    private long id;

    /**
     * 好友昵称
     */
    private String name;

    /**
     * 最后一条消息的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date timestamp;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 是否有未读消息
     */
    private boolean hasUnRead;

    /**
     * 最新一条聊天消息
     */
    private String oneNewMessage;
}
