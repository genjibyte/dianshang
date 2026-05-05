package com.example.ordertest.order;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@Epic("订单管理")
@Feature("订单查询")
@DisplayName("订单查询测试")
public class OrderQueryTest extends BaseTest {

    @Test
    @Story("根据订单号查询")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("根据订单号查询订单 - 验证所有字段正确")
    public void testGetOrderByOrderNo() {
        String orderNo = createStandardOrder(1L);

        Response response = orderApiClient.getOrder(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.orderNo", equalTo(orderNo))
                .body("data.userId", equalTo(1))
                .body("data.shopId", equalTo(1))
                .body("data.status", equalTo(0))
                .body("data.statusDesc", equalTo("待支付"))
                .body("data.deliveryAddress", notNullValue())
                .body("data.items", hasSize(1))
                .body("data.items[0].productName", equalTo("宫保鸡丁"))
                .body("data.items[0].unitPrice", equalTo(28.0F))
                .body("data.items[0].quantity", equalTo(2))
                .body("data.totalAmount", equalTo(56.0F))
                .body("data.createTime", notNullValue());
    }

    @Test
    @Story("查询不存在的订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("查询不存在的订单号 - 期望返回订单不存在")
    public void testGetOrderNotFound() {
        Response response = orderApiClient.getOrder("NONEXISTENT_ORDER_NO");

        response.then()
                .statusCode(200)
                .body("code", equalTo(2001));
    }

    @Test
    @Story("查询用户订单列表")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("查询用户订单列表 - 创建两个订单后验证列表数量")
    public void testGetUserOrders() {
        createStandardOrder(1L);
        createStandardOrder(1L);

        Response response = orderApiClient.getUserOrders(1L);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @Story("查询无订单用户")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("查询无订单的用户 - 期望返回空列表")
    public void testGetUserOrdersEmpty() {
        Response response = orderApiClient.getUserOrders(2L);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", hasSize(0));
    }

    @Test
    @Story("查询不存在的用户订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("查询不存在的用户订单 - 期望返回用户不存在")
    public void testGetOrdersUserNotFound() {
        Response response = orderApiClient.getUserOrders(999L);

        response.then()
                .statusCode(200)
                .body("code", equalTo(1001));
    }

    @Test
    @Story("查询已取消订单详情")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("查询已取消订单详情 - 验证取消原因和取消时间")
    public void testGetCancelledOrderDetail() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.cancelOrder(1L, orderNo, "临时有事");

        Response response = orderApiClient.getOrder(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.status", equalTo(4))
                .body("data.statusDesc", equalTo("已取消"))
                .body("data.cancelReason", equalTo("临时有事"))
                .body("data.cancelledTime", notNullValue());
    }
}
