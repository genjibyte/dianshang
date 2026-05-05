package com.example.ordertest.base;

import com.example.ordertest.client.OrderApiClient;
import com.example.ordertest.client.PaymentApiClient;
import com.example.ordertest.config.WireMockConfig;
import com.example.ordertest.data.mapper.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseTest {

    @Autowired
    protected TestOrderMapper testOrderMapper;

    @Autowired
    protected TestPaymentMapper testPaymentMapper;

    @Autowired
    protected TestProductMapper testProductMapper;

    @Autowired
    protected TestIdempotencyMapper testIdempotencyMapper;

    @Autowired
    protected TestUserMapper testUserMapper;

    @Autowired
    protected OrderApiClient orderApiClient;

    @Autowired
    protected PaymentApiClient paymentApiClient;

    @Autowired
    protected WireMockConfig wireMockConfig;

    @Autowired
    @Qualifier("paymentGatewayServer")
    protected WireMockServer paymentGatewayServer;

    @Autowired
    @Qualifier("pushNotificationServer")
    protected WireMockServer pushNotificationServer;

    /** Stores orderNos created during the test for cleanup */
    protected List<String> createdOrderNos = new ArrayList<>();

    /** Stores idempotency keys used during the test for cleanup */
    protected List<String> usedIdempotencyKeys = new ArrayList<>();

    @BeforeEach
    public void baseSetUp() {
        createdOrderNos.clear();
        usedIdempotencyKeys.clear();

        // Reset WireMock stubs to defaults
        wireMockConfig.resetAllStubs();

        // Reset product stock
        testProductMapper.resetStock(1L, 100);
        testProductMapper.resetStock(2L, 50);

        // Reset user balances
        testUserMapper.resetBalance(1L, new BigDecimal("1000.00"));
        testUserMapper.resetBalance(3L, new BigDecimal("0.50"));
    }

    @AfterEach
    public void baseTearDown() {
        // Clean up orders and related data created during the test
        for (String orderNo : createdOrderNos) {
            try {
                testPaymentMapper.deleteByOrderNo(orderNo);
            } catch (Exception ignored) {
            }
            try {
                testIdempotencyMapper.deleteByBizId(orderNo);
            } catch (Exception ignored) {
            }
            try {
                testOrderMapper.deleteItemsByOrderNo(orderNo);
            } catch (Exception ignored) {
            }
            try {
                testOrderMapper.deleteByOrderNo(orderNo);
            } catch (Exception ignored) {
            }
        }

        // Clean up idempotency records by key
        for (String key : usedIdempotencyKeys) {
            try {
                testIdempotencyMapper.deleteByKey(key, "CREATE_ORDER");
            } catch (Exception ignored) {
            }
            try {
                testIdempotencyMapper.deleteByKey(key, "PAY_ORDER");
            } catch (Exception ignored) {
            }
        }

        // Reset product stock
        testProductMapper.resetStock(1L, 100);
        testProductMapper.resetStock(2L, 50);

        // Reset user balances
        testUserMapper.resetBalance(1L, new BigDecimal("1000.00"));
        testUserMapper.resetBalance(3L, new BigDecimal("0.50"));
    }

    /**
     * Helper: build an order item map
     */
    protected Map<String, Object> buildItem(Long productId, Integer quantity) {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("quantity", quantity);
        return item;
    }

    /**
     * Helper: create a standard order and track it for cleanup.
     * Returns the orderNo.
     */
    protected String createStandardOrder(Long userId) {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 2));
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        io.restassured.response.Response response = orderApiClient.createOrder(
                userId, 1L, "北京市朝阳区测试地址1号", "测试订单", items, idempotencyKey);

        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }
        return orderNo;
    }

    /**
     * Helper: create an order and pay it. Returns the orderNo.
     */
    protected String createAndPayOrder(Long userId, Integer payMethod) {
        String orderNo = createStandardOrder(userId);
        String payKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);
        paymentApiClient.payOrder(userId, orderNo, payMethod, payKey);
        return orderNo;
    }
}
