package com.example.ordertest.idempotency;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Epic("系统健壮性")
@Feature("幂等性")
@DisplayName("幂等性测试")
public class IdempotencyTest extends BaseTest {

    @Test
    @Story("创建订单幂等")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("相同幂等键重复创建订单 - 验证返回同一订单号")
    public void testCreateOrderIdempotency() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String idempotencyKey = "IDEM_CREATE_" + UUID.randomUUID();
        usedIdempotencyKeys.add(idempotencyKey);

        // 第一次请求
        Response first = orderApiClient.createOrder(
                1L, 1L, "测试地址", "幂等测试", items, idempotencyKey);
        first.then().body("code", equalTo(200));
        String firstOrderNo = first.jsonPath().getString("data.orderNo");
        createdOrderNos.add(firstOrderNo);

        // 第二次请求（相同幂等键）
        Response second = orderApiClient.createOrder(
                1L, 1L, "测试地址", "幂等测试", items, idempotencyKey);
        second.then().body("code", equalTo(200));
        String secondOrderNo = second.jsonPath().getString("data.orderNo");

        assertEquals(firstOrderNo, secondOrderNo, "相同幂等键应返回相同订单号");
    }

    @Test
    @Story("支付订单幂等")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("相同幂等键重复支付订单 - 验证返回同一支付单号")
    public void testPayOrderIdempotency() {
        String orderNo = createStandardOrder(1L);
        String payKey = "IDEM_PAY_" + UUID.randomUUID();
        usedIdempotencyKeys.add(payKey);

        // 第一次支付
        Response first = paymentApiClient.payOrder(1L, orderNo, 1, payKey);
        first.then().body("code", equalTo(200));
        String firstPaymentNo = first.jsonPath().getString("data.paymentNo");

        // 第二次支付（相同幂等键）
        Response second = paymentApiClient.payOrder(1L, orderNo, 1, payKey);
        second.then().body("code", equalTo(200));
        String secondPaymentNo = second.jsonPath().getString("data.paymentNo");

        assertEquals(firstPaymentNo, secondPaymentNo, "相同幂等键应返回相同支付单号");
    }

    @Test
    @Story("不同幂等键创建不同订单")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("不同幂等键创建订单 - 验证生成不同订单号")
    public void testDifferentKeysCreateDifferentOrders() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));

        String key1 = "IDEM_DIFF_1_" + UUID.randomUUID();
        String key2 = "IDEM_DIFF_2_" + UUID.randomUUID();
        usedIdempotencyKeys.add(key1);
        usedIdempotencyKeys.add(key2);

        Response first = orderApiClient.createOrder(
                1L, 1L, "地址A", "备注", items, key1);
        String firstOrderNo = first.jsonPath().getString("data.orderNo");
        createdOrderNos.add(firstOrderNo);

        Response second = orderApiClient.createOrder(
                1L, 1L, "地址B", "备注", items, key2);
        String secondOrderNo = second.jsonPath().getString("data.orderNo");
        createdOrderNos.add(secondOrderNo);

        assertNotEquals(firstOrderNo, secondOrderNo, "不同幂等键应生成不同订单号");
    }

    @Test
    @Story("无幂等键创建订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("不传幂等键多次创建订单 - 验证每次都生成新订单")
    public void testCreateOrderWithoutIdempotencyKey() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));

        Response first = orderApiClient.createOrder(
                1L, 1L, "地址A", "备注", items, null);
        String firstOrderNo = first.jsonPath().getString("data.orderNo");
        if (firstOrderNo != null) createdOrderNos.add(firstOrderNo);

        Response second = orderApiClient.createOrder(
                1L, 1L, "地址B", "备注", items, null);
        String secondOrderNo = second.jsonPath().getString("data.orderNo");
        if (secondOrderNo != null) createdOrderNos.add(secondOrderNo);

        assertNotEquals(firstOrderNo, secondOrderNo, "不传幂等键应每次生成新订单");
    }
}
