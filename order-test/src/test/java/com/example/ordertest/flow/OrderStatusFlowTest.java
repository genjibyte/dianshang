package com.example.ordertest.flow;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Epic("订单管理")
@Feature("订单状态流转")
@DisplayName("订单状态流转测试")
public class OrderStatusFlowTest extends BaseTest {

    @Test
    @Story("完整订单生命周期")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("完整生命周期: 下单→支付→配送→完成")
    public void testFullOrderLifecycle() {
        // Step 1: 创建订单 → 待支付(0)
        String orderNo = createStandardOrder(1L);
        orderApiClient.getOrder(orderNo).then()
                .body("data.status", equalTo(0))
                .body("data.statusDesc", equalTo("待支付"));

        // Step 2: 支付 → 已支付(1)
        String payKey = java.util.UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);
        paymentApiClient.payOrder(1L, orderNo, 1, payKey);
        orderApiClient.getOrder(orderNo).then()
                .body("data.status", equalTo(1))
                .body("data.statusDesc", equalTo("已支付"))
                .body("data.paidTime", notNullValue());

        // Step 3: 配送 → 配送中(2)
        orderApiClient.deliverOrder(orderNo);
        orderApiClient.getOrder(orderNo).then()
                .body("data.status", equalTo(2))
                .body("data.statusDesc", equalTo("配送中"))
                .body("data.deliveredTime", notNullValue());

        // Step 4: 完成 → 已完成(3)
        orderApiClient.completeOrder(orderNo);
        orderApiClient.getOrder(orderNo).then()
                .body("data.status", equalTo(3))
                .body("data.statusDesc", equalTo("已完成"))
                .body("data.completedTime", notNullValue());
    }

    @Test
    @Story("取消已支付订单退还余额")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("取消已支付订单 - 验证余额恢复")
    public void testCancelAfterPayRefundsBalance() {
        BigDecimal initialBalance = new BigDecimal("1000.00");

        // 创建订单并支付（宫保鸡丁 28*2=56）
        String orderNo = createAndPayOrder(1L, 1);

        // 取消订单
        orderApiClient.cancelOrder(1L, orderNo, "余额退款测试");

        // 验证余额恢复
        Map<String, Object> user = testUserMapper.selectById(1L);
        BigDecimal afterBalance = new BigDecimal(user.get("BALANCE").toString());
        assertEquals(0, initialBalance.compareTo(afterBalance),
                "取消已支付订单后余额应恢复至原始值");
    }

    @Test
    @Story("未支付不能配送")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("未支付订单尝试配送 - 期望返回状态不允许")
    public void testCannotDeliverUnpaidOrder() {
        String orderNo = createStandardOrder(1L);

        Response response = orderApiClient.deliverOrder(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("未配送不能完成")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("未配送订单尝试完成 - 期望返回状态不允许")
    public void testCannotCompleteUndeliveredOrder() {
        String orderNo = createAndPayOrder(1L, 1);

        Response response = orderApiClient.completeOrder(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("已完成不能配送")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("已完成订单尝试再次配送 - 期望返回状态不允许")
    public void testCannotDeliverCompletedOrder() {
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.deliverOrder(orderNo);
        orderApiClient.completeOrder(orderNo);

        Response response = orderApiClient.deliverOrder(orderNo);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("已取消不能支付")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("已取消订单尝试支付 - 期望返回状态不允许")
    public void testCannotPayCancelledOrder() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.cancelOrder(1L, orderNo, "取消后支付测试");

        String payKey = java.util.UUID.randomUUID().toString();
        usedIdempotencyKeys.add(payKey);
        Response response = paymentApiClient.payOrder(1L, orderNo, 1, payKey);

        response.then()
                .statusCode(200)
                .body("code", equalTo(2002));
    }

    @Test
    @Story("库存恢复验证")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("取消订单后库存恢复 - 验证商品库存正确回滚")
    public void testStockRestoredAfterCancel() {
        int initialStock = ((Number) testProductMapper.selectById(1L).get("STOCK")).intValue();

        // 创建订单（扣减2个）
        String orderNo = createStandardOrder(1L);
        int afterOrderStock = ((Number) testProductMapper.selectById(1L).get("STOCK")).intValue();
        assertEquals(initialStock - 2, afterOrderStock, "下单后库存应减少");

        // 取消订单
        orderApiClient.cancelOrder(1L, orderNo, "库存恢复测试");
        int afterCancelStock = ((Number) testProductMapper.selectById(1L).get("STOCK")).intValue();
        assertEquals(initialStock, afterCancelStock, "取消后库存应恢复");
    }
}
