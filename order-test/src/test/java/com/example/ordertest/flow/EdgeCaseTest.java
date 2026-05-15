package com.example.ordertest.flow;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;

@Epic("系统健壮性")
@Feature("边界与异常场景")
@DisplayName("边界场景测试")
public class EdgeCaseTest extends BaseTest {

    @Test
    @Story("响应结构完整性")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("创建订单响应结构 - code/message/data/traceId 均存在")
    public void testResponseStructureComplete() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.getOrder(orderNo).then()
                .body("code", notNullValue())
                .body("message", notNullValue())
                .body("data", notNullValue())
                .body("traceId", notNullValue());
    }

    @Test
    @Story("缺少用户ID请求头")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("下单请求不传 X-User-Id 请求头 - 期望返回400")
    public void testCreateOrderMissingUserIdHeader() {
        Map<String, Object> body = new HashMap<>();
        body.put("shopId", 1);
        body.put("deliveryAddress", "测试地址");
        body.put("items", Collections.singletonList(buildItem(1L, 1)));
        io.restassured.RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(body)
                .when().post("/orders")
                .then().statusCode(200).body("code", equalTo(400));
    }

    @Test
    @Story("查询 traceId 每次不同")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("两次请求的 traceId 不同")
    public void testTraceIdUniquePerRequest() {
        String orderNo = createStandardOrder(1L);
        String traceId1 = orderApiClient.getOrder(orderNo).jsonPath().getString("traceId");
        String traceId2 = orderApiClient.getOrder(orderNo).jsonPath().getString("traceId");
        org.junit.jupiter.api.Assertions.assertNotEquals(traceId1, traceId2, "每次请求的traceId应不同");
    }

    @Test
    @Story("不存在店铺下单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("店铺不存在时下单 - 期望返回1002")
    public void testCreateOrderShopNotFound() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 999L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().statusCode(200).body("code", equalTo(1002));
    }

    @Test
    @Story("获取已完成订单详情")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("已完成订单详情 - 验证完成时间和状态")
    public void testGetCompletedOrderDetail() {
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.deliverOrder(orderNo);
        orderApiClient.completeOrder(orderNo);
        orderApiClient.getOrder(orderNo).then()
                .body("data.status", equalTo(3))
                .body("data.statusDesc", equalTo("已完成"))
                .body("data.completedTime", notNullValue());
    }

    @Test
    @Story("配送中状态验证")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("开始配送后 - 状态变为配送中且 deliveredTime 不为空")
    public void testDeliveringStatus() {
        String orderNo = createAndPayOrder(1L, 1);
        orderApiClient.deliverOrder(orderNo);
        orderApiClient.getOrder(orderNo).then()
                .body("data.status", equalTo(2))
                .body("data.statusDesc", equalTo("配送中"))
                .body("data.deliveredTime", notNullValue());
    }

    @Test
    @Story("取消原因保存正确")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("取消订单后 - cancelReason 字段与传入原因一致")
    public void testCancelReasonPersisted() {
        String orderNo = createStandardOrder(1L);
        String reason = "临时有紧急事情" + UUID.randomUUID().toString().substring(0, 4);
        orderApiClient.cancelOrder(1L, orderNo, reason);
        orderApiClient.getOrder(orderNo).then()
                .body("data.cancelReason", equalTo(reason));
    }

    @Test
    @Story("支付记录包含交易流水号")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("微信支付成功后 - transactionId 字段非空")
    public void testTransactionIdNotNullForThirdPartyPay() {
        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        paymentApiClient.payOrder(1L, orderNo, 2, key);
        paymentApiClient.getPaymentByOrderNo(orderNo).then()
                .body("data.transactionId", not(emptyOrNullString()));
    }

    @Test
    @Story("用户列表按时间倒序")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("用户订单列表 - 按创建时间倒序返回")
    public void testUserOrdersOrderedByTime() {
        String orderNo1 = createStandardOrder(1L);
        String orderNo2 = createStandardOrder(1L);
        Response response = orderApiClient.getUserOrders(1L);
        response.then().body("code", equalTo(200)).body("data", hasSize(greaterThanOrEqualTo(2)));
        // 最新的订单在最前面
        String firstOrderNo = response.jsonPath().getString("data[0].orderNo");
        org.junit.jupiter.api.Assertions.assertEquals(orderNo2, firstOrderNo, "最新订单应排在第一位");
    }

    @Test
    @Story("支付方式描述正确")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("支付宝支付 - payMethodDesc 为支付宝")
    public void testAlipayMethodDesc() {
        String orderNo = createStandardOrder(1L);
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        paymentApiClient.payOrder(1L, orderNo, 3, key).then()
                .body("data.payMethod", equalTo(3))
                .body("data.payMethodDesc", equalTo("支付宝"));
    }

    @Test
    @Story("错误响应不包含data")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("请求失败时 - data 字段为 null")
    public void testErrorResponseDataIsNull() {
        orderApiClient.getOrder("NONEXISTENT_ORDER_NO").then()
                .body("code", equalTo(2001))
                .body("data", nullValue());
    }

    @Test
    @Story("重复取消幂等")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("已取消订单再次取消 - 返回状态不允许而非系统错误")
    public void testCancelIdempotentResponse() {
        String orderNo = createStandardOrder(1L);
        orderApiClient.cancelOrder(1L, orderNo, "第一次取消");
        orderApiClient.cancelOrder(1L, orderNo, "第二次取消").then()
                .body("code", equalTo(2002))
                .body("traceId", notNullValue());
    }
}
