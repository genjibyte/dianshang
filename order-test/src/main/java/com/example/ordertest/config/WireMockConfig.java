package com.example.ordertest.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
public class WireMockConfig {

    private WireMockServer paymentGatewayServer;
    private WireMockServer pushNotificationServer;

    @Bean(name = "paymentGatewayServer")
    public WireMockServer paymentGatewayServer() {
        paymentGatewayServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(9090)
        );
        paymentGatewayServer.start();
        registerPaymentGatewayStubs(paymentGatewayServer);
        return paymentGatewayServer;
    }

    @Bean(name = "pushNotificationServer")
    public WireMockServer pushNotificationServer() {
        pushNotificationServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(9091)
        );
        pushNotificationServer.start();
        registerPushNotificationStubs(pushNotificationServer);
        return pushNotificationServer;
    }

    public static void registerPaymentGatewayStubs(WireMockServer server) {
        server.stubFor(post(urlEqualTo("/api/v1/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"transactionId\":\"TXN_TEST_001\"}")));

        server.stubFor(post(urlEqualTo("/api/v1/refund"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"refundId\":\"REF_TEST_001\"}")));
    }

    public static void registerPushNotificationStubs(WireMockServer server) {
        server.stubFor(post(urlEqualTo("/api/v1/push"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true}")));
    }

    public void resetAllStubs() {
        if (paymentGatewayServer != null && paymentGatewayServer.isRunning()) {
            paymentGatewayServer.resetAll();
            registerPaymentGatewayStubs(paymentGatewayServer);
        }
        if (pushNotificationServer != null && pushNotificationServer.isRunning()) {
            pushNotificationServer.resetAll();
            registerPushNotificationStubs(pushNotificationServer);
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
