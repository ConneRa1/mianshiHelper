package com.yupi.mianshiya.controller;

import com.yupi.mianshiya.common.BaseResponse;
import com.yupi.mianshiya.common.ResultUtils;
import com.yupi.mianshiya.fastgpt.FastGPTClient;
import com.yupi.mianshiya.fastgpt.model.PaginationRecords;
import com.yupi.mianshiya.model.dto.fastgpt.ChatRequest;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.service.UserService;
import com.yupi.mianshiya.utils.SnowFlakeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * ai接口
 */
@RestController
@RequestMapping("/chat")
@Slf4j
@AllArgsConstructor
public class FastGptController {
    private final FastGPTClient fastGPTClient;
    private final UserService userService;


    /**
     * 生成唯一的chatId
     *
     * @return chatID
     */

    public Long newChat(HttpServletRequest request) {
        long chatId = SnowFlakeUtil.nextId();
        User loginUser = userService.getLoginUser(request);

        //插入到用户的会话表中
        return SnowFlakeUtil.nextId(); // 使用 UUID 生成唯一 ID
    }

    /**
     * 非流式响应接口
     *
     * @param request 用户输入的内容
     * @return FastGPT 的响应
     */
    @PostMapping("/normal-response")
    public BaseResponse<FastGPTClient.FastGPTResponse>  normalResponse(@RequestBody ChatRequest request) {
        return ResultUtils.success(fastGPTClient.normalResponse(request));
    }


    /**
     * 获取会话历史记录
     *
     * @param chatId 会话ID
     */
    @GetMapping("/pagination-records")
    public void getPaginationRecords(@RequestParam String chatId) {
        //校验chatid是否属于该用户
        //校验库里是否有对话记录
        //从api中去请求获得，存库
        PaginationRecords paginationRecords = fastGPTClient.getPaginationRecords(chatId);

    }
}
