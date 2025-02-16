package com.yupi.mianshiya.fastgpt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yupi.mianshiya.fastgpt.model.PaginationRecords;
import com.yupi.mianshiya.model.dto.fastgpt.ChatRequest;
import com.yupi.mianshiya.model.entity.ChatMessage;
import com.yupi.mianshiya.service.ChatMessageService;
import com.yupi.mianshiya.utils.SnowFlakeUtil;
import lombok.AllArgsConstructor;
import okhttp3.*;
import com.google.gson.Gson;
import okio.BufferedSource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
public class FastGPTClient {
    private static final String API_URL = "https://cloud.fastgpt.cn/api";
    private static final String API_KEY = "fastgpt-d62pufplzv3RRdDJRvNwCXezI4OH6WmygT641748Mj50cyFub1aSVCguUkEJg"; // 注意包含fastgpt-前缀
    private final ChatMessageService chatMessageService;

    // 创建全局的 OkHttpClient 实例
    private static final OkHttpClient client =  new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时
            .readTimeout(30, TimeUnit.SECONDS)     // 读取超时
            .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时
            .build();


    /**
     * 非流式响应
     *
     * @param chatRequest
     */
    public String normalResponse(ChatRequest chatRequest){
        // 构建请求体
        Long chatId = chatRequest.getChatId();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatId", chatId); // 唯一会话ID
        requestBody.put("stream", false);
        requestBody.put("detail", false);

        //构造返回体
        ChatMessage chatMessage = ChatMessage.builder().chatId(chatId).build();

        // 用户消息
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("user", chatRequest.getContent()));
        chatMessage.setId(SnowFlakeUtil.nextId());
        chatMessage.setObj("Human");
        chatMessage.setContent(chatRequest.getContent());
        chatMessageService.save(chatMessage);


        requestBody.put("messages", messages);

        Map<String, String> variables = new HashMap<>();
        variables.put("uid", "111111");
        variables.put("name", "张三");
        requestBody.put("variables", variables);


        // 发送请求
        Request request = new Request.Builder()
                .url(API_URL+"/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        new Gson().toJson(requestBody),
                        MediaType.parse("application/json")
                ))
                .build();

        // 使用 try-with-resources 关闭 Response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: HTTP " + response.code());
            }

            String responseBody = response.body().string();
            System.out.println("完整响应:\n" + responseBody);

            // 解析响应
            FastGPTResponse result = new Gson().fromJson(responseBody, FastGPTResponse.class);
            String answer=null;
            if (result.choices != null && !result.choices.isEmpty()) {
                answer = result.choices.get(0).message.content;
                System.out.println("\n最终答案: " + answer);
            }
            chatMessage.setId(SnowFlakeUtil.nextId());
            chatMessage.setObj("AI");
            chatMessage.setContent(result.choices.get(0).message.content);
            chatMessageService.save(chatMessage);
            return answer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 流式响应
     *
     * @param chatRequest
     */
    public void streamResponse(ChatRequest chatRequest, WebSocketSession session){
        // 构建流式请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatId", chatRequest.getChatId()); // 唯一会话ID
        requestBody.put("stream", true); // 启用流式
        requestBody.put("detail", false);

        //构造返回体
        ChatMessage chatMessage = ChatMessage.builder().chatId(chatRequest.getChatId()).build();

        // 用户消息
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("user",  chatRequest.getContent()));
        requestBody.put("messages", messages);
        chatMessage.setId(SnowFlakeUtil.nextId());
        chatMessage.setObj("Human");
        chatMessage.setContent(chatRequest.getContent());
        chatMessageService.save(chatMessage);

        // 构建请求
        Request request = new Request.Builder()
                .url(API_URL+"/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Accept", "text/event-stream") // 重要：声明接受事件流
                .post(RequestBody.create(
                        new Gson().toJson(requestBody),
                        MediaType.parse("application/json")
                ))
                .build();

        // 异步处理流式响应
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.err.println("HTTP错误: " + response.code());
                    return;
                }

                try (BufferedSource source = response.body().source()) {
                    StringBuilder fullResponse = new StringBuilder();

                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line == null) continue;

                        // 处理事件流格式：data: {...}
                        if (line.startsWith("data: ")) {
                            String json = line.substring(6).trim();
                            //System.out.println("原始JSON: " + json);
                            if (json.isEmpty()) continue;
                            if ("[DONE]".equals(json)) {break;}
                            // 解析流式数据块


                            try {
                                JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
                                JsonArray choices = jsonObj.getAsJsonArray("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    JsonObject delta = choices.get(0).getAsJsonObject()
                                            .getAsJsonObject("delta");

                                    // 检查 content 是否为 null
                                    if (delta.has("content") && !delta.get("content").isJsonNull()) {
                                        String content = delta.get("content").getAsString();
                                        if (content != null) {
                                            fullResponse.append(content);
                                            System.out.print(content);
                                            System.out.flush();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("解析异常，JSON内容: " + json);
                                e.printStackTrace();
                            }
                        }
                    }
                    chatMessage.setId(SnowFlakeUtil.nextId());
                    chatMessage.setObj("AI");
                    chatMessage.setContent(String.valueOf(fullResponse));
                    chatMessageService.save(chatMessage);
                }
            }
        });

    }


    /**
     * 得到会话的历史记录
     *
     * @param chatId
     */
    public PaginationRecords getPaginationRecords(String chatId){
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatId", chatId);
        requestBody.put("offset", 0);
        requestBody.put("pageSize", 100);
        requestBody.put("loadCustomFeedbacks", false);
        requestBody.put("appId", "67ab40c79404094d7ec91b82");


        // 发送请求
        Request request = new Request.Builder()
                .url(API_URL+"/core/chat/getPaginationRecords")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        new Gson().toJson(requestBody),
                        MediaType.parse("application/json")
                ))
                .build();

        // 使用 try-with-resources 关闭 Response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: HTTP " + response.code());
            }

            String responseBody = response.body().string();
            System.out.println("完整响应:\n" + responseBody);

            // 解析响应
            PaginationRecords result = new Gson().fromJson(responseBody, PaginationRecords.class);
            System.out.println("Code: " + result.getCode());
            System.out.println("Total Items: " + result.getData().getTotal());
            result.getData().getList().forEach(item -> {
                System.out.println("ID: " + item.get_id());
                System.out.println("Object: " + item.getObj());
                item.getValue().forEach(value -> {
                    System.out.println("Content: " + value.getText());
                });
            });
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 响应对象定义
    public static class FastGPTResponse {
        String id;
        String model;
        Usage usage;
        List<Choice> choices;
    }

    static class Usage {
        int prompt_tokens;
        int completion_tokens;
        int total_tokens;
    }

    static class Choice {
        Message message;
        String finish_reason;
        int index;
    }

    static class Message {
        String role;
        String content;
    }


    // 流式响应数据结构
    static class StreamResponse {
        String id;
        List<StreamChoice> choices;
    }

    static class StreamChoice {
        StreamDelta delta;
        String finish_reason;
        int index;
    }

    static class StreamDelta {
        String content;
    }

    // 创建消息辅助方法
    private static Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}