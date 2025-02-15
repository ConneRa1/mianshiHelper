package com.yupi.mianshiya.fastgpt.model;

import lombok.Data;

import java.util.List;

@Data
public class ChatItem {
    private String _id;
    private String dataId;
    private String obj;
    private List<ValueItem> value;
    private List<String> customFeedbacks;
    private Integer llmModuleAccount;
    private transient List<String> totalQuoteList;
    private Double totalRunningTime;
    private Integer historyPreviewLength;
}