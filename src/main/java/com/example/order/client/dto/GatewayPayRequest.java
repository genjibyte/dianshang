package com.example.order.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GatewayPayRequest {
    private String orderNo;
    private BigDecimal amount;
    private Integer payMethod;
    private String notifyUrl;
}
