package com.example.order.client.impl;

import com.example.order.client.PaymentGatewayClient;
import com.example.order.client.dto.GatewayPayRequest;
import com.example.order.client.dto.GatewayPayResponse;
import com.example.order.client.dto.GatewayRefundRequest;
import com.example.order.client.dto.GatewayRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class PaymentGatewayClientImpl implements PaymentGatewayClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentGatewayClientImpl(
            RestTemplate restTemplate,
            @Value("${external.payment-gateway.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public GatewayPayResponse pay(GatewayPayRequest request) {
        log.info("调用支付网关: orderNo={}, amount={}, method={}",
                request.getOrderNo(), request.getAmount(), request.getPayMethod());

        ResponseEntity<GatewayPayResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/pay",
                request,
                GatewayPayResponse.class
        );

        GatewayPayResponse body = response.getBody();
        log.info("支付网关响应: orderNo={}, success={}, transactionId={}",
                request.getOrderNo(),
                body != null && body.isSuccess(),
                body != null ? body.getTransactionId() : null);
        return body;
    }

    @Override
    public GatewayRefundResponse refund(GatewayRefundRequest request) {
        log.info("调用退款网关: orderNo={}, transactionId={}, amount={}",
                request.getOrderNo(), request.getTransactionId(), request.getRefundAmount());

        ResponseEntity<GatewayRefundResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/refund",
                request,
                GatewayRefundResponse.class
        );

        GatewayRefundResponse body = response.getBody();
        log.info("退款网关响应: orderNo={}, success={}",
                request.getOrderNo(), body != null && body.isSuccess());
        return body;
    }
}
