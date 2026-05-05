package com.example.ordertest.client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Component
public class OrderApiClient {

    @Step("创建订单 - userId: {userId}, shopId: {shopId}")
    public Response createOrder(Long userId, Long shopId, String deliveryAddress,
                                String remark, List<Map<String, Object>> items,
                                String idempotencyKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("shopId", shopId);
        body.put("deliveryAddress", deliveryAddress);
        body.put("remark", remark);
        body.put("items", items);

        return given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", userId)
                .header("Idempotency-Key", idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())
                .body(body)
                .when()
                .post("/orders");
    }

    @Step("创建订单(使用默认幂等键) - userId: {userId}, shopId: {shopId}")
    public Response createOrder(Long userId, Long shopId, String deliveryAddress,
                                String remark, List<Map<String, Object>> items) {
        return createOrder(userId, shopId, deliveryAddress, remark, items, UUID.randomUUID().toString());
    }

    @Step("查询订单详情 - orderNo: {orderNo}")
    public Response getOrder(String orderNo) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/orders/{orderNo}", orderNo);
    }

    @Step("查询用户订单列表 - userId: {userId}")
    public Response getUserOrders(Long userId) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/orders/user/{userId}", userId);
    }

    @Step("取消订单 - userId: {userId}, orderNo: {orderNo}, reason: {reason}")
    public Response cancelOrder(Long userId, String orderNo, String reason) {
        Map<String, Object> body = new HashMap<>();
        body.put("orderNo", orderNo);
        body.put("reason", reason);

        return given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", userId)
                .body(body)
                .when()
                .post("/orders/cancel");
    }

    @Step("订单配送 - orderNo: {orderNo}")
    public Response deliverOrder(String orderNo) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .post("/orders/{orderNo}/deliver", orderNo);
    }

    @Step("订单完成 - orderNo: {orderNo}")
    public Response completeOrder(String orderNo) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .post("/orders/{orderNo}/complete", orderNo);
    }
}
