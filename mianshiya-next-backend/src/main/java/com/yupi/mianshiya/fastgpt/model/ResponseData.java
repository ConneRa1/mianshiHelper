package com.yupi.mianshiya.fastgpt.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseData {
    private List<ChatItem> list;
    private int total;
}