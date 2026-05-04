package com.example.order.client.impl;

import com.example.order.client.PushNotificationClient;
import com.example.order.client.dto.PushMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class PushNotificationClientImpl implements PushNotificationClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PushNotificationClientImpl(
            RestTemplate restTemplate,
            @Value("${external.push-notification.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void push(PushMessageRequest request) {
        log.info("发送推送通知: userId={}, title={}, bizType={}, bizId={}",
                request.getUserId(), request.getTitle(), request.getBizType(), request.getBizId());
        try {
            restTemplate.postForEntity(
                    baseUrl + "/api/v1/push",
                    request,
                    Void.class
            );
        } catch (Exception e) {
            log.error("推送通知发送失败: userId={}, title={}", request.getUserId(), request.getTitle(), e);
        }
    }
}
