package com.yz.minded.job;

import com.yz.minded.WebSocket.ChatEndPoint;
import com.yz.minded.pojo.ChatMessages;
import com.yz.minded.service.ChatMessagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 缓存中消息持久化
 * @author yz
 */
@Slf4j
@Component
public class MessageCacheEndurance {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ChatMessagesService chatMessagesService;

    // 每天凌晨 3 点执行消息持久化
    @Scheduled(cron = "0 0 3 * * ? ")
    public void doMessageCacheEndurance() {
        log.info("开始持久化用户聊天消息");
        Set<String> chatUserKeys = ChatEndPoint.chatUserKeys;
        if (chatUserKeys == null || chatUserKeys.isEmpty()) {
            log.info("没有需要持久化的聊天消息");
            return;
        }

        // 拷贝一份，避免遍历时 remove 触发问题
        List<String> keys = new ArrayList<>(chatUserKeys);
        int success = 0, fail = 0;

        for (String messageKey : keys) {
            try {
                Long size = redisTemplate.opsForList().size(messageKey);
                if (size == null || size <= 0) {
                    ChatEndPoint.chatUserKeys.remove(messageKey);
                    continue;
                }

                // 【修复】range 是闭区间，end 要 -1，原来传 size 会多读一个空槽
                List<Object> resultMessages = redisTemplate.opsForList().range(messageKey, 0, size - 1);
                if (resultMessages == null || resultMessages.isEmpty()) {
                    ChatEndPoint.chatUserKeys.remove(messageKey);
                    continue;
                }

                List<ChatMessages> chatMessagesList = new ArrayList<>();
                for (Object resultMessage : resultMessages) {
                    ChatMessages chatMessage = new ChatMessages();
                    BeanUtils.copyProperties(resultMessage, chatMessage);
                    chatMessagesList.add(chatMessage);
                }

                // 批量落库
                boolean result = chatMessagesService.saveBatch(chatMessagesList);
                if (result) {
                    redisTemplate.delete(messageKey);
                    ChatEndPoint.chatUserKeys.remove(messageKey);
                    success++;
                } else {
                    // 落库失败，保留 key，下次定时任务重试
                    fail++;
                    log.warn("消息落库失败，保留缓存 key={}", messageKey);
                }
            } catch (Exception e) {
                fail++;
                log.error("持久化消息异常 key={}", messageKey, e);
            }
        }

        log.info("持久化用户聊天消息完成，成功 {} 个，失败 {} 个", success, fail);
    }
}