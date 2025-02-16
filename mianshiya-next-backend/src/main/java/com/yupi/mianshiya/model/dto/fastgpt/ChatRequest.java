package com.yupi.mianshiya.model.dto.fastgpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 会话id
     */
    Long chatId;
    /**
     * 回答内容
     */
    String content;
}
