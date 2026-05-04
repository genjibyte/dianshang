package com.example.order.client.dto;

import lombok.Data;

@Data
public class GatewayRefundResponse {
    private boolean success;
    private String refundId;
    private String errorCode;
    private String errorMessage;
}
