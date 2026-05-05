package com.example.ordertest.payment;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;

@Epic("支付管理")
@Feature("订单支付")
@DisplayName("支付订单测试")
public class PayOrderTest extends BaseTest {

    @Test
    @Story("余额支付成功")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("余额支付 - 验证支付记录和订单状态更新")
    public void testPayWithBalanceSuccess() {
        String orderNo = createStandardOrder(1L);
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(1L, orderNo, 1, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.paymentNo", notNullValue())
                .body("data.orderNo", equalTo(orderNo))
                .body("data.amount", equalTo(56.0F))
                .body("data.payMethod", equalTo(1))
                .body("data.payMethodDesc", equalTo("余额"))
                .body("data.status", equalTo(1))
                .body("data.statusDesc", equalTo("支付成功"))
                .body("data.paidTime", notNullValue());

        // 验证订单状态变为已支付
        orderApiClient.getOrder(orderNo).then()
                .body("data.status", equalTo(1))
                .body("data.paidTime", notNullValue());
    }

    @Test
    @Story("微信支付成功")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("微信支付 - WireMock模拟网关成功响应")
    public void testPayWithWechatSuccess() {
        String orderNo = createStandardOrder(1L);
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(1L, orderNo, 2, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.payMethod", equalTo(2))
                .body("data.payMethodDesc", equalTo("微信支付"))
                .body("data.transactionId", notNullValue());
    }

    @Test
    @Story("支付宝支付成功")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("支付宝支付 - WireMock模拟网关成功响应")
    public void testPayWithAlipaySuccess() {
        String orderNo = createStandardOrder(1L);
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(1L, orderNo, 3, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.payMethod", equalTo(3))
                .body("data.payMethodDesc", equalTo("支付宝"));
    }

    @Test
    @Story("余额不足")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("余额不足时支付 - 期望返回余额不足错误")
    public void testPayBalanceNotEnough() {
        // userId=3 余额只有0.50，订单金额56
        String orderNo = createStandardOrder(3L);
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(3L, orderNo, 1, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(3002));
    }

    @Test
    @Story("重复支付已支付订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("已支付订单再次支付 - 期望返回状态不允许")
    public void testPayAlreadyPaidOrder() {
        String orderNo = createAndPayOrder(1L, 1);
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(1L, orderNo, 1, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("支付已取消订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("已取消订单支付 - 期望返回状态不允许")
    public void testPayCancelledOrder() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.cancelOrder(1L, orderNo, "不想吃了");
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(1L, orderNo, 1, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("支付不存在的订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("支付不存在的订单 - 期望返回订单不存在")
    public void testPayOrderNotFound() {
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(1L, "NONEXISTENT_ORDER", 1, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2001));
    }

    @Test
    @Story("非本人支付订单")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("非本人支付订单 - 期望返回无权操作")
    public void testPayOrderByOtherUser() {
        String orderNo = createStandardOrder(1L);
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);

        Response response = paymentApiClient.payOrder(2L, orderNo, 1, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2003));
    }
}
