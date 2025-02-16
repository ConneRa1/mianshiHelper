package com.yupi.mianshiya.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.mianshiya.common.BaseResponse;
import com.yupi.mianshiya.common.ErrorCode;
import com.yupi.mianshiya.common.ResultUtils;
import com.yupi.mianshiya.exception.BusinessException;
import com.yupi.mianshiya.fastgpt.FastGPTClient;
import com.yupi.mianshiya.fastgpt.model.PaginationRecords;
import com.yupi.mianshiya.model.dto.fastgpt.ChatRequest;
import com.yupi.mianshiya.model.entity.ChatMessage;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.model.entity.UserChat;
import com.yupi.mianshiya.service.ChatMessageService;
import com.yupi.mianshiya.service.UserChatService;
import com.yupi.mianshiya.service.UserService;
import com.yupi.mianshiya.utils.SnowFlakeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

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
    private final UserChatService userChatService;
    private final ChatMessageService chatMessageService;




    /**
     * 新建对话
     *
     * @return chatID
     */
    @PostMapping("/newChat")
    public BaseResponse<ChatMessage> newChat(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String category = params.get("category");
        long chatId = SnowFlakeUtil.nextId();
        User loginUser = userService.getLoginUser(request);
        userChatService.save(UserChat.builder()
            .chatId(chatId)
            .userId(loginUser.getId())
            .title(category + SnowFlakeUtil.nextId())
            .category(category)
            .build());
        //获得开场白
        ChatMessage welcomeText = fastGPTClient.initChat(chatId);

        return ResultUtils.success(welcomeText);
    }

    /**
     * 非流式响应接口
     *
     * @param request 用户输入的内容
     * @return FastGPT 的响应
     */
    @PostMapping("/normal-response")
    public BaseResponse<String>  normalResponse(@RequestBody ChatRequest request) {
        return ResultUtils.success(fastGPTClient.normalResponse(request));
    }

    /**
     * 获取历史的会话
     *
     */
    @GetMapping("/getRecords")
    public BaseResponse<IPage<UserChat>> getPaginationRecords(@RequestParam(defaultValue = "1") long current,
                                                                 @RequestParam(defaultValue = "20") long pageSize,
                                                                 HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        //分页查询
        Page<UserChat> page = new Page<>(current, pageSize);
        IPage<UserChat> chats =  userChatService.lambdaQuery().eq(UserChat::getUserId, loginUser.getId()).page(page);

        return ResultUtils.success(chats);
    }


    /**
     * 获取会话历史记录
     *
     * @param chatId 会话ID
     */
    @GetMapping("/pagination-records")
    public BaseResponse<IPage<ChatMessage>> getPaginationRecords( @RequestParam Long chatId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long pageSize,
            HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 校验chatId是否属于该用户
        UserChat userChat = userChatService.lambdaQuery()
                .eq(UserChat::getChatId, chatId)
                .eq(UserChat::getUserId, loginUser.getId())
                .one();
        if (userChat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无权访问该会话");
        }
        
        // 分页查询聊天记录
        Page<ChatMessage> page = new Page<>(current, pageSize);
        IPage<ChatMessage> chatMessages = chatMessageService.lambdaQuery()
                .eq(ChatMessage::getChatId, chatId)
                .orderByAsc(ChatMessage::getCreateTime)
                .page(page);
        
        return ResultUtils.success(chatMessages);
        }
}
