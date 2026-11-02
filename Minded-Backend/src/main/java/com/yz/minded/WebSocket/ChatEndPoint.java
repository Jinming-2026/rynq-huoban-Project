package com.yz.minded.WebSocket;

import com.alibaba.fastjson.JSON;
import com.yz.minded.WebSocket.pojo.ResultMessage;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.RedisKeyConstant;
import com.yz.minded.config.GetHttpSessionConfig;
import com.yz.minded.contant.UserConstant;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.User;
import com.yz.minded.service.ChatMessagesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 配置端点类
 * @author yz
 */
@Component
@ServerEndpoint(value = "/chat/{receiverId}", configurator = GetHttpSessionConfig.class)
public class ChatEndPoint {

    /** 所有在线用户：userId -> Session */
    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(ChatEndPoint.class);

    /** 聊天用户的 key（线程安全） */
    public static final Set<String> chatUserKeys = ConcurrentHashMap.newKeySet();

    private static RedisTemplate<String, Object> redisTemplate;

    private HttpSession httpSession;

    @Autowired
    public void setYourService(RedisTemplate<String, Object> redisTemplate) {
        ChatEndPoint.redisTemplate = redisTemplate;
    }

    private static RedisTemplate<String, Object> getRedisTemplate() {
        if (redisTemplate == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(ChatEndPoint.class);
        }
        return redisTemplate;
    }

    private static ChatMessagesService chatMessagesService;

    @Autowired
    public void setChatMessagesService(ChatMessagesService chatMessagesService) {
        ChatEndPoint.chatMessagesService = chatMessagesService;
    }

    private static ChatMessagesService getChatMessagesService() {
        if (chatMessagesService == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(ChatEndPoint.class);
        }
        return chatMessagesService;
    }

    /**
     * 建立 WebSocket 连接成功后触发
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig,
                       @PathParam("receiverId") String receiverId) {
        // 1、从握手阶段拿 HttpSession
        this.httpSession = (HttpSession) endpointConfig.getUserProperties().get(HttpSession.class.getName());
        if (this.httpSession == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "WebSocket 未登录");
        }
        User user = (User) this.httpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "WebSocket 未登录");
        }

        // 2、上线登记
        long userId = user.getId();
        onlineUsers.put(userId, session);

        // 3、建立连接时，把对方发给我的消息标记为已读
        try {
            long targetId = Long.parseLong(receiverId);
            boolean result = getChatMessagesService().updateReadStatus(targetId, userId, user);
            log.info("WebSocket 建立连接: userId={}, targetId={}, markRead={}", userId, targetId, result);
        } catch (NumberFormatException e) {
            log.warn("receiverId 非法: {}", receiverId);
        }
    }

    /**
     * 浏览器发送消息到服务端，该方法被调用
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            ResultMessage msg = JSON.parseObject(message, ResultMessage.class);
            if (msg == null || msg.getSenderId() <= 0 || msg.getReceiverId() <= 0) {
                log.warn("消息格式非法: {}", message);
                return;
            }

            long senderId = msg.getSenderId();
            long receiverId = msg.getReceiverId();

            // 记录当前聊天 key（供定时任务落库）
            String key = RedisKeyConstant.chatMessage(senderId, receiverId);
            chatUserKeys.add(key);

            // 复制一份用于推送
            ResultMessage resultMsg = new ResultMessage();
            BeanUtils.copyProperties(msg, resultMsg);

            // 推送给接收者（双方都在线才算已读）
            Session receiverSession = onlineUsers.get(receiverId);
            boolean markedRead = false;
            if (receiverSession != null && receiverSession.isOpen()) {
                try {
                    receiverSession.getBasicRemote().sendText(JSON.toJSONString(resultMsg));
                    markedRead = true;
                } catch (Exception e) {
                    log.warn("WebSocket 推送失败 receiverId={}", receiverId, e);
                }
            }
            // 0-已读，1-未读
            msg.setReadStatus(markedRead ? 0 : 1);

            // 消息写入 Redis 缓存
            getRedisTemplate().opsForList().rightPush(key, msg);

        } catch (Exception e) {
            // WebSocket 回调里不要再抛业务异常（会被 onError 吞掉，且对端收不到），只记日志
            log.error("处理 WebSocket 消息失败: {}", message, e);
        }
    }

    /**
     * 断开连接时
     */
    @OnClose
    public void onClose(Session session) {
        try {
            if (this.httpSession == null) return;
            User user = (User) this.httpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
            if (user == null) return;
            onlineUsers.remove(user.getId());
            log.info("WebSocket 关闭连接: userId={}", user.getId());
        } catch (Exception e) {
            log.warn("关闭 WebSocket 连接异常", e);
        }
    }

    /**
     * 发生错误时触发
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket 错误", throwable);
    }
}