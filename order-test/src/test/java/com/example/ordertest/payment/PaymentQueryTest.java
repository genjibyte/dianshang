package com.example.ordertest.payment;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@Epic("支付管理")
@Feature("支付查询")
@DisplayName("支付查询测试")
public class PaymentQueryTest extends BaseTest {

    @Test
    @Story("查询支付记录")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("查询已支付订单的支付记录 - 验证所有字段")
    public void testGetPaymentSuccess() {
        String orderNo = createAndPayOrder(1L, 1);

        Response response = paymentApiClient.getPaymentByOrderNo(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.paymentNo", notNullValue())
                .body("data.orderNo", equalTo(orderNo))
                .body("data.amount", equalTo(56.0F))
                .body("data.payMethod", equalTo(1))
                .body("data.status", equalTo(1))
                .body("data.paidTime", notNullValue());
    }

    @Test
    @Story("查询不存在的支付记录")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("查询未支付订单的支付记录 - 期望返回支付记录不存在")
    public void testGetPaymentNotFound() {
        String orderNo = createStandardOrder(1L);

        Response response = paymentApiClient.getPaymentByOrderNo(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(3001));
    }

    @Test
    @Story("查询退款后的支付记录")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("查询已退款订单的支付记录 - 验证支付状态为已退款")
    public void testGetRefundedPayment() {
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.cancelOrder(1L, orderNo, "退款测试");

        Response response = paymentApiClient.getPaymentByOrderNo(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.status", equalTo(3))
                .body("data.statusDesc", equalTo("已退款"));
    }
}
