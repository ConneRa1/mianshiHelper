package com.yupi.mianshiya.fastgpt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yupi.mianshiya.fastgpt.model.PaginationRecords;
import okhttp3.*;
import com.google.gson.Gson;
import okio.BufferedSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class FastGPTClient {
    private static final String API_URL = "https://cloud.fastgpt.cn/api";
    private static final String API_KEY = "fastgpt-d62pufplzv3RRdDJRvNwCXezI4OH6WmygT641748Mj50cyFub1aSVCguUkEJg"; // 注意包含fastgpt-前缀

    // 创建全局的 OkHttpClient 实例
    private static final OkHttpClient client =  new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时
            .readTimeout(30, TimeUnit.SECONDS)     // 读取超时
            .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时
            .build();


    /**
     * 非流式响应
     *
     * @param content
     */
    public void normalResponse(String content){
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatId", 114513); // 唯一会话ID
        requestBody.put("stream", false);
        requestBody.put("detail", false);

        // 用户消息
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("user", content));

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
            if (result.choices != null && !result.choices.isEmpty()) {
                String answer = result.choices.get(0).message.content;
                System.out.println("\n最终答案: " + answer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 流式响应
     *
     * @param content
     */
    public void streamResponse(String content){
        // 构建流式请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatId", 114514); // 唯一会话ID
        requestBody.put("stream", true); // 启用流式
        requestBody.put("detail", false);

        // 用户消息
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("user", content));

        requestBody.put("messages", messages);

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

                    System.out.println("\n\n完整响应内容：\n" + fullResponse);
                }
            }
        });

//        // 保持主线程运行（演示用）
//        try { Thread.sleep(Long.MAX_VALUE); }
//        catch (InterruptedException e) { e.printStackTrace(); }
    }


    /**
     * 得到会话的历史记录
     *
     * @param chatId
     */
    public void getPaginationRecords(String chatId){
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        FastGPTClient client = new FastGPTClient();
//        client.normalResponse("1.redis的IO模型更高效，使用事件驱动的IO多路复用。2.提供的数据结构更丰富，更好地满足业务需求");
        client.getPaginationRecords("114513");
    }

    // 响应对象定义
    static class FastGPTResponse {
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