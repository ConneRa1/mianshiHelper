package com.yupi.mianshiya.fastgpt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yupi.mianshiya.fastgpt.FastGPTClient;
import com.yupi.mianshiya.model.dto.fastgpt.ChatRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class FastGPTWebSocketHandler extends TextWebSocketHandler {
    private final FastGPTClient fastGPTClient;


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();

        String chatId = jsonObject.get("chatId").getAsString(); // 获取 chatId
        String content = jsonObject.get("content").getAsString();
        ChatRequest request=ChatRequest.builder().chatId(Long.valueOf(chatId)).content(content).build();
        // 调用 FastGPT 的流式响应方法
        fastGPTClient.streamResponse(request, session);
    }
}