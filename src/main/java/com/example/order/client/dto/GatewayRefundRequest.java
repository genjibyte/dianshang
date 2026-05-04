package com.example.order.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GatewayRefundRequest {
    private String orderNo;
    private String transactionId;
    private BigDecimal refundAmount;
    private String reason;
}
