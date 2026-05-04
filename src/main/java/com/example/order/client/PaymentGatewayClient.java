package com.example.order.client;

import com.example.order.client.dto.GatewayPayRequest;
import com.example.order.client.dto.GatewayPayResponse;
import com.example.order.client.dto.GatewayRefundRequest;
import com.example.order.client.dto.GatewayRefundResponse;

public interface PaymentGatewayClient {

    GatewayPayResponse pay(GatewayPayRequest request);

    GatewayRefundResponse refund(GatewayRefundRequest request);
}
