package com.yz.minded.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yz.minded.pojo.ChatMessages;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.vo.ChatMessagesVo;

import java.util.List;

/**
* @author admin
* @description 针对表【chat_messages(用户私聊消息表)】的数据库操作Service
* @createDate 2024-10-29 18:58:02
*/
public interface ChatMessagesService extends IService<ChatMessages> {

    /**
     * 获取历史消息
     *
     * @param senderId   发送用户id
     * @param receiverId 接收用户id
     * @return 历史消息集合
     */
    List<ChatMessagesVo> getHistoryMessages(long senderId, long receiverId);

    /**
     * 获取用户之间最新的一条信息
     *
     * @param senderUser   s
     * @param receiverUser r
     * @return 是否有未读消息
     */
    ChatMessages getOneNewMessage(User senderUser, User receiverUser);

    /**
     * 更新已读消息状态
     *
     * @param senderId 发送者 id
     * @param receiverId    接收者 id（查看）
     * @return true - 修改成功
     */
    // 接口 ChatMessagesService 里
    boolean updateReadStatus(long senderId, long receiverId, User loginUser);

}
