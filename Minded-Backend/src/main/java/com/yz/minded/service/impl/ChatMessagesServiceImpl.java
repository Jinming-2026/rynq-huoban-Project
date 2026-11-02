package com.yz.minded.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yz.minded.WebSocket.pojo.ResultMessage;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.RedisKeyConstant;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.mapper.ChatMessagesMapper;
import com.yz.minded.pojo.ChatMessages;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.vo.ChatMessagesVo;
import com.yz.minded.service.ChatMessagesService;
import com.yz.minded.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * @description 针对表【chat_messages(用户私聊消息表)】的数据库操作Service实现
 * @createDate 2024-10-29 18:58:02
 */
@Service
public class ChatMessagesServiceImpl extends ServiceImpl<ChatMessagesMapper, ChatMessages>
        implements ChatMessagesService {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取历史消息
     */
    @Transactional
    @Override
    public List<ChatMessagesVo> getHistoryMessages(long senderId, long receiverId) {
        // 1. 参数校验
        if (senderId <= 0 || receiverId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 从数据库查询已保存的历史消息
        QueryWrapper<ChatMessages> chatMessagesQueryWrapper = new QueryWrapper<>();
        chatMessagesQueryWrapper
                .eq("sender_id", senderId).eq("receiver_id", receiverId)
                .or()
                .eq("receiver_id", senderId).eq("sender_id", receiverId);
        List<ChatMessages> chatMessagesFromSQL = this.list(chatMessagesQueryWrapper);
        if (chatMessagesFromSQL == null) {
            chatMessagesFromSQL = new ArrayList<>();
        }

        // 3. 从缓存中获取历史消息
        List<ChatMessages> chatMessagesFromCache = this.getChatMessagesFromCache(senderId, receiverId);
        if (chatMessagesFromCache != null && !chatMessagesFromCache.isEmpty()) {
            chatMessagesFromSQL.addAll(chatMessagesFromCache);
        }

        // 4. 组装返回内容
        List<ChatMessagesVo> chatMessagesVoList = new ArrayList<>();   // ← 修复①：补 List
        chatMessagesFromSQL.forEach(chatMessages -> {
            User senderUser = userService.getById(chatMessages.getSenderId());
            String senderUserAvatar = senderUser == null ? null : senderUser.getAvatarUrl();   // ← 加判空
            ChatMessagesVo vo = new ChatMessagesVo();
            vo.setSenderId(chatMessages.getSenderId());
            vo.setReceiverId(String.valueOf(chatMessages.getReceiverId()));
            vo.setAvatar(senderUserAvatar);
            vo.setMessage(chatMessages.getMessage());
            vo.setTimestamp(chatMessages.getTimestamp());
            vo.setReadStatus(1);
            chatMessagesVoList.add(vo);
        });

        // 5. 按时间升序返回
        return chatMessagesVoList.stream()
                .sorted((c1, c2) -> {
                    long time = c1.getTimestamp().getTime() - c2.getTimestamp().getTime();
                    if (time == 0) return 0;
                    return time > 0 ? 1 : -1;
                }).toList();
    }

    /**
     * 获取最新一条的消息
     */
    @Override
    public ChatMessages getOneNewMessage(User senderUser, User receiverUser) {
        if (senderUser == null || receiverUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long senderUserId = senderUser.getId();
        long receiverUserId = receiverUser.getId();

        QueryWrapper<ChatMessages> messagesQueryWrapper = new QueryWrapper<>();
        messagesQueryWrapper
                .eq("sender_id", senderUserId).eq("receiver_id", receiverUserId)
                .or()
                .eq("sender_id", receiverUserId).eq("receiver_id", senderUserId);
        messagesQueryWrapper.orderByDesc("timestamp");
        List<ChatMessages> chatMessagesFromSQL = this.list(messagesQueryWrapper);
        boolean sqlIsEmpty = (chatMessagesFromSQL == null || chatMessagesFromSQL.isEmpty());

        List<ChatMessages> chatMessagesFromCache = this.getChatMessagesFromCache(senderUserId, receiverUserId);
        if ((chatMessagesFromCache == null || chatMessagesFromCache.isEmpty()) && sqlIsEmpty) {
            return null;
        }

        List<ChatMessages> chatMessagesList = new ArrayList<>();
        if (chatMessagesFromSQL != null) {
            chatMessagesList.addAll(chatMessagesFromSQL);
        }
        if (chatMessagesFromCache != null) {
            chatMessagesList.addAll(chatMessagesFromCache);
        }

        List<ChatMessages> resultMessageList = chatMessagesList.stream()
                .sorted((c1, c2) -> {
                    long time = c2.getTimestamp().getTime() - c1.getTimestamp().getTime();
                    if (time == 0) return 0;
                    return time > 0 ? 1 : -1;
                }).toList();

        return resultMessageList.isEmpty() ? null : resultMessageList.get(0);
    }

    /**
     * 修改用户之间的消息读取状态
     */
    @Override
    public boolean updateReadStatus(long senderId, long receiverId, User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 【建议恢复】只有接收者本人才能把消息标成已读
        // long loginUserId = loginUser.getId();
        // if (loginUserId != receiverId) return false;

        // ========== 1、更新数据库中的消息状态 ==========
        QueryWrapper<ChatMessages> messagesQueryWrapper = new QueryWrapper<>();
        messagesQueryWrapper
                .eq("sender_id", senderId).eq("receiver_id", receiverId).ne("read_status", 0)
                .or()
                .eq("sender_id", receiverId).eq("receiver_id", senderId).ne("read_status", 0);
        ChatMessages chatMessages = new ChatMessages();
        chatMessages.setReadStatus(0);
        List<ChatMessages> chatMessages1 = list(messagesQueryWrapper);
        if (chatMessages1 != null && !chatMessages1.isEmpty()) {
            this.update(chatMessages, messagesQueryWrapper);
        }

        // ========== 2、更新缓存中的消息状态 ==========
        String key1 = RedisKeyConstant.chatMessage(senderId, receiverId);
        String key2 = RedisKeyConstant.chatMessage(receiverId, senderId);

        List<ChatMessages> cacheChatMessage = this.getChatMessagesFromCache(senderId, receiverId);
        if (cacheChatMessage == null || cacheChatMessage.isEmpty()) {
            return true;
        }

        redisTemplate.delete(key1);
        redisTemplate.delete(key2);

        for (ChatMessages cacheMessage : cacheChatMessage) {
            String key = RedisKeyConstant.chatMessage(cacheMessage.getSenderId(), cacheMessage.getReceiverId());
            if (cacheMessage.getReadStatus() == null
                    || (cacheMessage.getReadStatus() == 1 && cacheMessage.getReceiverId().equals(receiverId))) {
                cacheMessage.setReadStatus(0);
            }
            ResultMessage resultMessage = new ResultMessage();
            BeanUtils.copyProperties(cacheMessage, resultMessage);
            redisTemplate.opsForList().rightPush(key, resultMessage);
        }
        return true;
    }

    /**
     * 从缓存中获取两个用户之间的所有消息
     */
    private List<ChatMessages> getChatMessagesFromCache(long senderId, long receiverId) {
        List<ChatMessages> chatMessagesList = new ArrayList<>();
        String key1 = RedisKeyConstant.chatMessage(senderId, receiverId);
        String key2 = RedisKeyConstant.chatMessage(receiverId, senderId);

        Long size1 = redisTemplate.opsForList().size(key1);
        Long size2 = redisTemplate.opsForList().size(key2);

        if ((size1 != null && size1 > 0) || (size2 != null && size2 > 0)) {
            // range 是闭区间，end 要 -1
            List<Object> sToR = (size1 != null && size1 > 0)
                    ? redisTemplate.opsForList().range(key1, 0, size1 - 1) : null;
            List<Object> rToS = (size2 != null && size2 > 0)
                    ? redisTemplate.opsForList().range(key2, 0, size2 - 1) : null;

            if (sToR != null) {
                for (Object o : sToR) {
                    ChatMessages m = new ChatMessages();
                    BeanUtils.copyProperties((ResultMessage) o, m);
                    chatMessagesList.add(m);
                }
            }
            if (rToS != null) {
                for (Object o : rToS) {
                    ChatMessages m = new ChatMessages();
                    BeanUtils.copyProperties((ResultMessage) o, m);
                    chatMessagesList.add(m);
                }
            }
        }
        return chatMessagesList;
    }
}