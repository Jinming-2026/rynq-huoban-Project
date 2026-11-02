package com.yz.minded.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 开启WebSocket支持，配置 websocket
 */
@Configuration
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 自动注册带有 @ServerEndpoint 注解的 WebSocket 端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(8192)      // 设置消息字节数大小
                .setSendBufferSizeLimit(8192)       // 设置消息缓存大小
                .setSendTimeLimit(10000);           // 设置消息发送时间限制（毫秒）
    }
}