package com.yupi.mianshiya.fastgpt.model;

import lombok.Data;
import java.util.List;

@Data
public class PaginationRecords {
    private int code;
    private String statusText;
    private String message;
    private ResponseData data;

}
