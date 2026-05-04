package com.example.order.client;

import com.example.order.client.dto.PushMessageRequest;

public interface PushNotificationClient {

    void push(PushMessageRequest request);
}
