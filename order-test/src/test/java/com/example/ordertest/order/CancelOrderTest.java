package com.example.ordertest.order;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@Epic("订单管理")
@Feature("取消订单")
@DisplayName("取消订单测试")
public class CancelOrderTest extends BaseTest {

    @Test
    @Story("取消待支付订单")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("取消待支付订单 - 验证状态变为已取消")
    public void testCancelPendingOrderSuccess() {
        String orderNo = createStandardOrder(1L);

        Response response = orderApiClient.cancelOrder(1L, orderNo, "不想吃了");

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.status", equalTo(4))
                .body("data.cancelReason", equalTo("不想吃了"))
                .body("data.cancelledTime", notNullValue());
    }

    @Test
    @Story("取消已支付订单并退款")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("取消已支付订单 - 验证状态为已取消且触发退款")
    public void testCancelPaidOrderWithRefund() {
        String orderNo = createAndPayOrder(1L, 1);

        Response response = orderApiClient.cancelOrder(1L, orderNo, "商家太慢了");

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.status", equalTo(4))
                .body("data.cancelReason", equalTo("商家太慢了"));
    }

    @Test
    @Story("重复取消订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("重复取消已取消的订单 - 期望返回错误")
    public void testCancelAlreadyCancelledOrder() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.cancelOrder(1L, orderNo, "第一次取消");

        Response response = orderApiClient.cancelOrder(1L, orderNo, "第二次取消");

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("取消已完成订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("取消已完成订单 - 期望返回状态不允许")
    public void testCancelCompletedOrder() {
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.deliverOrder(orderNo);
        orderApiClient.completeOrder(orderNo);

        Response response = orderApiClient.cancelOrder(1L, orderNo, "想退款");

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("非本人取消订单")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("非本人取消订单 - 期望返回无权操作")
    public void testCancelOrderByOtherUser() {
        String orderNo = createStandardOrder(1L);

        Response response = orderApiClient.cancelOrder(2L, orderNo, "别人的单");

        response.then()
                .statusCode(200)
                .body("code", equalTo(2003));
    }

    @Test
    @Story("取消不存在的订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("取消不存在的订单 - 期望返回订单不存在")
    public void testCancelOrderNotFound() {
        Response response = orderApiClient.cancelOrder(1L, "NONEXISTENT_ORDER", "原因");

        response.then()
                .statusCode(200)
                .body("code", equalTo(2001));
    }

    @Test
    @Story("取消配送中订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("取消配送中的订单 - 期望返回状态不允许")
    public void testCancelDeliveringOrder() {
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.deliverOrder(orderNo);

        Response response = orderApiClient.cancelOrder(1L, orderNo, "不要了");

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }
}
