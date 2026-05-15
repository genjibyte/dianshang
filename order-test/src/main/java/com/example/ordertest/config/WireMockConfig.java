package com.example.ordertest.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
@Configuration
public class WireMockConfig {

    @Value("${wiremock.enabled:true}")
    private boolean wireMockEnabled;

    @Value("${wiremock.payment-gateway-port:9090}")
    private int paymentGatewayPort;

    @Value("${wiremock.push-notification-port:9091}")
    private int pushNotificationPort;

    private WireMockServer paymentGatewayServer;
    private WireMockServer pushNotificationServer;

    @Bean(name = "paymentGatewayServer")
    public WireMockServer paymentGatewayServer() {
        if (!wireMockEnabled) {
            log.info("WireMock 已禁用（当前为 dev 环境），跳过支付网关 Mock 启动");
            return null;
        }
        paymentGatewayServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(paymentGatewayPort));
        paymentGatewayServer.start();
        registerPaymentStubs(paymentGatewayServer);
        log.info("WireMock 支付网关已启动: port={}", paymentGatewayPort);
        return paymentGatewayServer;
    }

    @Bean(name = "pushNotificationServer")
    public WireMockServer pushNotificationServer() {
        if (!wireMockEnabled) {
            log.info("WireMock 已禁用（当前为 dev 环境），跳过推送服务 Mock 启动");
            return null;
        }
        pushNotificationServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(pushNotificationPort));
        pushNotificationServer.start();
        registerPushStubs(pushNotificationServer);
        log.info("WireMock 推送服务已启动: port={}", pushNotificationPort);
        return pushNotificationServer;
    }

    // =================== 默认桩注册 ===================

    public static void registerPaymentStubs(WireMockServer server) {
        // 支付成功
        server.stubFor(post(urlEqualTo("/api/v1/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"transactionId\":\"TXN_MOCK_001\"}")));
        // 退款成功
        server.stubFor(post(urlEqualTo("/api/v1/refund"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"refundId\":\"REF_MOCK_001\"}")));
    }

    public static void registerPushStubs(WireMockServer server) {
        server.stubFor(post(urlEqualTo("/api/v1/push"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true}")));
    }

    public void resetAllStubs() {
        if (paymentGatewayServer != null && paymentGatewayServer.isRunning()) {
            paymentGatewayServer.resetAll();
            registerPaymentStubs(paymentGatewayServer);
        }
        if (pushNotificationServer != null && pushNotificationServer.isRunning()) {
            pushNotificationServer.resetAll();
            registerPushStubs(pushNotificationServer);
        }
    }

    @PreDestroy
    public void stopServers() {
        if (paymentGatewayServer != null && paymentGatewayServer.isRunning()) {
            paymentGatewayServer.stop();
        }
        if (pushNotificationServer != null && pushNotificationServer.isRunning()) {
            pushNotificationServer.stop();
        }
    }
}
