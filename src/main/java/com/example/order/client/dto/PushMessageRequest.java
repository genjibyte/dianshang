package com.example.order.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PushMessageRequest {
    private Long userId;
    private String title;
    private String content;
    private String bizType;
    private String bizId;
}
