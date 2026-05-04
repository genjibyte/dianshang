package com.example.order.client.dto;

import lombok.Data;

@Data
public class GatewayPayResponse {
    private boolean success;
    private String transactionId;
    private String errorCode;
    private String errorMessage;
}
