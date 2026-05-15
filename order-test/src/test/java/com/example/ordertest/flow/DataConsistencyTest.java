package com.example.ordertest.flow;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Epic("数据一致性")
@Feature("业务数据一致性验证")
@DisplayName("数据一致性测试")
public class DataConsistencyTest extends BaseTest {

    @Test
    @Story("支付后订单金额与支付金额一致")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("支付金额与订单总金额完全一致")
    public void testPaymentAmountMatchesOrderAmount() {
        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        paymentApiClient.payOrder(1L, orderNo, 1, key);

        float orderAmount = orderApiClient.getOrder(orderNo)
                .jsonPath().getFloat("data.totalAmount");
        float paymentAmount = paymentApiClient.getPaymentByOrderNo(orderNo)
                .jsonPath().getFloat("data.amount");

        assertEquals(orderAmount, paymentAmount, 0.001f, "支付金额应与订单金额一致");
    }

    @Test
    @Story("余额支付后账户余额减少")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("余额支付 - 用户余额准确减少支付金额")
    public void testBalanceDeductedAfterPayment() {
        BigDecimal before = new BigDecimal(testUserMapper.selectById(1L).get("BALANCE").toString());
        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        paymentApiClient.payOrder(1L, orderNo, 1, key).then().body("code", equalTo(200));

        BigDecimal after = new BigDecimal(testUserMapper.selectById(1L).get("BALANCE").toString());
        BigDecimal diff = before.subtract(after);
        assertEquals(0, diff.compareTo(new BigDecimal("56.00")), "余额应减少56元（宫保鸡丁28*2）");
    }

    @Test
    @Story("取消后余额恢复")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("余额支付后取消 - 余额完整退回")
    public void testBalanceRestoredAfterCancel() {
        BigDecimal before = new BigDecimal(testUserMapper.selectById(1L).get("BALANCE").toString());
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.cancelOrder(1L, orderNo, "退款一致性测试");

        BigDecimal after = new BigDecimal(testUserMapper.selectById(1L).get("BALANCE").toString());
        assertEquals(0, before.compareTo(after), "取消后余额应完整恢复");
    }

    @Test
    @Story("订单明细数量与请求一致")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("创建订单后订单明细数量与请求商品项一致")
    public void testOrderItemCountConsistency() {
        String orderNo = createStandardOrder(1L);

        Response response = orderApiClient.getOrder(orderNo);
        response.then()
                .body("data.items", hasSize(1))
                .body("data.items[0].productId", equalTo(1))
                .body("data.items[0].quantity", equalTo(2));
    }

    @Test
    @Story("并发下单库存不超卖")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("顺序创建多个订单 - 库存不超卖")
    public void testStockNotOversoldSequential() {
        // 商品2初始库存50，每次扣1个，创建5个订单
        for (int i = 0; i < 5; i++) {
            String key = UUID.randomUUID().toString();
            usedIdempotencyKeys.add(key);
            Response r = orderApiClient.createOrder(1L, 1L, "测试地址" + i, "",
                    java.util.Collections.singletonList(buildItem(2L, 1)), key);
            String orderNo = r.jsonPath().getString("data.orderNo");
            if (orderNo != null) createdOrderNos.add(orderNo);
        }

        int remaining = ((Number) testProductMapper.selectById(2L).get("STOCK")).intValue();
        assertEquals(45, remaining, "创建5个订单后库存应为45");
    }

    @Test
    @Story("取消后库存恢复")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("取消订单后库存准确恢复")
    public void testStockRestoredOnCancel() {
        int before = ((Number) testProductMapper.selectById(1L).get("STOCK")).intValue();
        String orderNo = createStandardOrder(1L);
        orderApiClient.cancelOrder(1L, orderNo, "库存一致性测试");
        int after = ((Number) testProductMapper.selectById(1L).get("STOCK")).intValue();
        assertEquals(before, after, "取消后库存应恢复");
    }

    @Test
    @Story("支付后订单有支付时间")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("支付成功后 - 订单的 paidTime 字段不为空")
    public void testOrderPaidTimeSetAfterPayment() {
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.getOrder(orderNo).then()
                .body("data.paidTime", notNullValue());
    }

    @Test
    @Story("取消订单有取消时间")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("取消订单后 - cancelledTime 字段不为空")
    public void testOrderCancelledTimeSet() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.cancelOrder(1L, orderNo, "时间字段测试");
        orderApiClient.getOrder(orderNo).then()
                .body("data.cancelledTime", notNullValue());
    }

    @Test
    @Story("支付单号唯一")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("两次不同订单的支付单号不同")
    public void testPaymentNosAreUnique() {
        String orderNo1 = createStandardOrder(1L);
        String orderNo2 = createStandardOrder(1L);
        String key1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key1);
        usedIdempotencyKeys.add(key2);

        paymentApiClient.payOrder(1L, orderNo1, 1, key1);
        paymentApiClient.payOrder(1L, orderNo2, 1, key2);

        String payNo1 = paymentApiClient.getPaymentByOrderNo(orderNo1).jsonPath().getString("data.paymentNo");
        String payNo2 = paymentApiClient.getPaymentByOrderNo(orderNo2).jsonPath().getString("data.paymentNo");
        assertNotEquals(payNo1, payNo2, "不同订单的支付单号应不同");
    }

    @Test
    @Story("响应包含traceId")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("所有响应中包含 traceId 链路追踪字段")
    public void testResponseContainsTraceId() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.getOrder(orderNo).then()
                .body("traceId", notNullValue());
    }
}
