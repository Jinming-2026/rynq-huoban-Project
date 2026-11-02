package com.yz.minded.common;

public class RedisKeyConstant {
    private static final String CHAT_MSG = "chat_message:sender_id:%d:receiver_id:%d";

    public static String chatMessage(long senderId, long receiverId) {
        return String.format(CHAT_MSG, senderId, receiverId);
    }
}