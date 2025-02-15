package com.yupi.mianshiya.config;

import com.yupi.mianshiya.fastgpt.FastGPTWebSocketHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final FastGPTWebSocketHandler fastGPTWebSocketHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(fastGPTWebSocketHandler, "/ws/fastgpt")
                .setAllowedOrigins("*"); // 允许所有来源
    }
}