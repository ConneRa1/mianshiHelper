package com.yupi.mianshiya.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.mianshiya.mapper.ChatMessageMapper;
import com.yupi.mianshiya.model.entity.ChatMessage;
import com.yupi.mianshiya.service.ChatMessageService;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_message】的数据库操作Service实现
* @createDate 2025-02-15 22:43:29
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService {

}




