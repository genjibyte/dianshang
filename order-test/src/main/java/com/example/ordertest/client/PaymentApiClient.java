package com.example.ordertest.client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Component
public class PaymentApiClient {

    @Step("支付订单 - userId: {userId}, orderNo: {orderNo}, payMethod: {payMethod}")
    public Response payOrder(Long userId, String orderNo, Integer payMethod, String idempotencyKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("orderNo", orderNo);
        body.put("payMethod", payMethod);

        return given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", userId)
                .header("Idempotency-Key", idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())
                .body(body)
                .when()
                .post("/payments");
    }

    @Step("支付订单(使用默认幂等键) - userId: {userId}, orderNo: {orderNo}, payMethod: {payMethod}")
    public Response payOrder(Long userId, String orderNo, Integer payMethod) {
        return payOrder(userId, orderNo, payMethod, UUID.randomUUID().toString());
    }

    @Step("查询订单支付信息 - orderNo: {orderNo}")
    public Response getPaymentByOrderNo(String orderNo) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/payments/order/{orderNo}", orderNo);
    }
}
