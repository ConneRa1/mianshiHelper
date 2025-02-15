package com.yupi.mianshiya.model.dto.fastgpt;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Data
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 会话id
     */
    String chatId;
    /**
     * 回答内容
     */
    String content;
}
