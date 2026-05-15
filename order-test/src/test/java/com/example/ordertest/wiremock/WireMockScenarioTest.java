package com.example.ordertest.wiremock;

import com.example.ordertest.base.BaseTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collections;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;

@Epic("系统健壮性")
@Feature("外部服务Mock")
@DisplayName("WireMock 场景测试")
public class WireMockScenarioTest extends BaseTest {

    @Autowired
    @Qualifier("paymentGatewayServer")
    private WireMockServer paymentGatewayServer;

    @Test
    @Story("支付网关返回成功")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("微信支付 - 网关返回成功，验证支付流程正常")
    public void testWechatPayGatewaySuccess() {
        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);

        paymentApiClient.payOrder(1L, orderNo, 2, key)
                .then()
                .body("code", equalTo(200))
                .body("data.transactionId", equalTo("TXN_MOCK_001"))
                .body("data.payMethod", equalTo(2));
    }

    @Test
    @Story("支付网关返回失败")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("微信支付 - 网关返回失败，验证返回支付网关错误")
    public void testWechatPayGatewayFail() {
        paymentGatewayServer.stubFor(post(urlEqualTo("/api/v1/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":false,\"errorCode\":\"INSUFFICIENT_FUNDS\",\"errorMessage\":\"账户余额不足\"}")));

        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);

        paymentApiClient.payOrder(1L, orderNo, 2, key)
                .then()
                .body("code", equalTo(3004));
    }

    @Test
    @Story("支付网关超时")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("支付宝支付 - 网关延迟响应，验证超时处理")
    public void testAlipayGatewayTimeout() {
        paymentGatewayServer.stubFor(post(urlEqualTo("/api/v1/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":false,\"errorMessage\":\"Gateway timeout\"}")
                        .withFixedDelay(5000)));

        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);

        Response response = paymentApiClient.payOrder(1L, orderNo, 3, key);
        response.then().body("code", not(equalTo(200)));
    }

    @Test
    @Story("退款网关返回成功")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("取消已支付订单 - 退款网关返回成功，验证退款流程")
    public void testRefundGatewaySuccess() {
        String orderNo = createAndPayOrder(1L, 2);

        orderApiClient.cancelOrder(1L, orderNo, "测试退款")
                .then()
                .body("code", equalTo(200))
                .body("data.status", equalTo(4));

        paymentApiClient.getPaymentByOrderNo(orderNo)
                .then()
                .body("data.status", equalTo(3))
                .body("data.statusDesc", equalTo("已退款"));
    }

    @Test
    @Story("退款网关返回失败")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("取消已支付订单 - 退款网关失败，验证返回退款失败错误")
    public void testRefundGatewayFail() {
        paymentGatewayServer.stubFor(post(urlEqualTo("/api/v1/refund"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":false,\"errorMessage\":\"Refund rejected\"}")));

        String orderNo = createAndPayOrder(1L, 2);

        orderApiClient.cancelOrder(1L, orderNo, "退款失败测试")
                .then()
                .body("code", equalTo(3006));
    }

    @Test
    @Story("推送服务调用验证")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("创建订单后推送服务被调用一次")
    public void testPushNotificationCalledOnOrderCreate() {
        String orderNo = createStandardOrder(1L);

        pushNotificationServer.verify(
                moreThanOrExactly(1),
                postRequestedFor(urlEqualTo("/api/v1/push"))
        );
    }

    @Test
    @Story("支付后推送通知")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("支付成功后推送服务被调用")
    public void testPushNotificationCalledOnPaySuccess() {
        String orderNo = createStandardOrder(1L);
        pushNotificationServer.resetRequests();

        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        paymentApiClient.payOrder(1L, orderNo, 1, key)
                .then().body("code", equalTo(200));

        pushNotificationServer.verify(
                moreThanOrExactly(1),
                postRequestedFor(urlEqualTo("/api/v1/push"))
        );
    }

    @Test
    @Story("网关返回500异常")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("支付网关返回500 - 验证系统正确处理异常")
    public void testPaymentGateway500Error() {
        paymentGatewayServer.stubFor(post(urlEqualTo("/api/v1/pay"))
                .willReturn(aResponse().withStatus(500)));

        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);

        Response response = paymentApiClient.payOrder(1L, orderNo, 2, key);
        response.then().body("code", not(equalTo(200)));
    }

    @Test
    @Story("余额支付不调用网关")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("余额支付 - 验证不调用第三方支付网关")
    public void testBalancePayDoesNotCallGateway() {
        String orderNo = createStandardOrder(1L);
        paymentGatewayServer.resetRequests();

        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        paymentApiClient.payOrder(1L, orderNo, 1, key)
                .then().body("code", equalTo(200));

        paymentGatewayServer.verify(0, postRequestedFor(urlEqualTo("/api/v1/pay")));
    }
}
