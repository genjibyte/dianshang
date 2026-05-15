package com.example.ordertest.order;

import com.example.ordertest.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;

@Epic("订单管理")
@Feature("创建订单")
@DisplayName("创建订单测试")
public class CreateOrderTest extends BaseTest {

    @Test
    @Story("正常创建订单")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("正常创建订单 - 验证响应码、订单号、状态、商品项和总金额")
    public void testCreateOrderSuccess() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 2));
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);
        Response response = orderApiClient.createOrder(
                1L, 1L, "北京市朝阳区幸福路100号", "少放辣", items, idempotencyKey);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.orderNo", notNullValue())
                .body("data.status", equalTo(0))
                .body("data.shopId", equalTo(1))
                .body("data.userId", equalTo(1))
                .body("data.deliveryAddress", equalTo("北京市朝阳区幸福路100号"))
                .body("data.remark", equalTo("少放辣"))
                .body("data.items", hasSize(1))
                .body("data.items[0].productId", equalTo(1))
                .body("data.items[0].quantity", equalTo(2))
                .body("data.totalAmount", equalTo(56.0F));
    }

    @Test
    @Story("多商品创建订单")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("多商品创建订单 - 验证总金额计算正确")
    public void testCreateOrderWithMultipleItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(buildItem(1L, 2)); // 28*2=56
        items.add(buildItem(2L, 3)); // 25*3=75
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "多放醋", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.items", hasSize(2))
                .body("data.totalAmount", equalTo(131.0F));
    }

    @Test
    @Story("创建订单返回订单号非空")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("创建订单 - 验证订单号格式以 ORD 开头")
    public void testCreateOrderNoFormat() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then()
                .body("code", equalTo(200))
                .body("data.orderNo", startsWith("ORD"));
    }

    @Test
    @Story("创建订单状态为待支付")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("创建订单 - 验证初始状态为0（待支付）")
    public void testCreateOrderInitialStatus() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then()
                .body("code", equalTo(200))
                .body("data.status", equalTo(0))
                .body("data.statusDesc", equalTo("待支付"));
    }

    @Test
    @Story("创建订单小计金额正确")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("创建订单 - 验证每项商品小计 = 单价 × 数量")
    public void testCreateOrderItemSubtotal() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 3));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then()
                .body("code", equalTo(200))
                .body("data.items[0].unitPrice", equalTo(28.0F))
                .body("data.items[0].quantity", equalTo(3))
                .body("data.items[0].subtotal", equalTo(84.0F))
                .body("data.totalAmount", equalTo(84.0F));
    }

    @Test
    @Story("下单后库存减少")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("创建订单 - 验证商品库存被正确扣减")
    public void testCreateOrderDeductsStock() {
        int stockBefore = ((Number) testProductMapper.selectById(1L).get("STOCK")).intValue();
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 2));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().body("code", equalTo(200));
        int stockAfter = ((Number) testProductMapper.selectById(1L).get("STOCK")).intValue();
        org.junit.jupiter.api.Assertions.assertEquals(stockBefore - 2, stockAfter, "库存应减少2");
    }

    @Test
    @Story("空备注创建订单")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("备注为空时创建订单 - 验证正常创建")
    public void testCreateOrderWithEmptyRemark() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", null, items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().body("code", equalTo(200)).body("data.orderNo", notNullValue());
    }

    @Test
    @Story("店铺已关闭")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("店铺已关闭时下单 - 期望返回错误码1003")
    public void testCreateOrderShopClosed() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 2L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().statusCode(200).body("code", equalTo(1003));
    }

    @Test
    @Story("商品已下架")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("商品已下架时下单 - 期望返回错误码1005")
    public void testCreateOrderProductOffShelf() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(5L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().statusCode(200).body("code", equalTo(1005));
    }

    @Test
    @Story("库存不足")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("库存为0时下单 - 期望返回错误码1007")
    public void testCreateOrderStockNotEnough() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(4L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().statusCode(200).body("code", equalTo(1007));
    }

    @Test
    @Story("库存数量超限")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("购买数量超过库存时下单 - 期望返回库存不足")
    public void testCreateOrderQuantityExceedsStock() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(2L, 999));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().statusCode(200).body("code", equalTo(1007));
    }

    @Test
    @Story("参数校验失败")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("缺少商品列表 - 期望返回400")
    public void testCreateOrderInvalidParams() {
        Map<String, Object> body = new HashMap<>();
        body.put("shopId", 1);
        body.put("deliveryAddress", "测试地址");
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        io.restassured.RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .header("X-User-Id", 1L)
                .header("Idempotency-Key", key)
                .body(body)
                .when().post("/orders")
                .then().statusCode(200).body("code", equalTo(400));
    }

    @Test
    @Story("收货地址为空")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("收货地址为空时下单 - 期望返回400")
    public void testCreateOrderEmptyAddress() {
        Map<String, Object> body = new HashMap<>();
        body.put("shopId", 1);
        body.put("deliveryAddress", "");
        body.put("items", Collections.singletonList(buildItem(1L, 1)));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        io.restassured.RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .header("X-User-Id", 1L)
                .header("Idempotency-Key", key)
                .body(body)
                .when().post("/orders")
                .then().statusCode(200).body("code", equalTo(400));
    }

    @Test
    @Story("用户不存在")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("用户不存在时下单 - 期望返回错误码1001")
    public void testCreateOrderUserNotFound() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(999L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().statusCode(200).body("code", equalTo(1001));
    }

    @Test
    @Story("商品不属于该店铺")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("商品不属于该店铺 - 期望返回错误码1006")
    public void testCreateOrderProductNotInShop() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(6L, 1));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        Response response = orderApiClient.createOrder(1L, 1L, "测试地址", "", items, key);
        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) createdOrderNos.add(orderNo);
        response.then().statusCode(200).body("code", equalTo(1006));
    }

    @Test
    @Story("商品数量为0")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("商品数量为0时下单 - 期望返回400")
    public void testCreateOrderZeroQuantity() {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1);
        item.put("quantity", 0);
        Map<String, Object> body = new HashMap<>();
        body.put("shopId", 1);
        body.put("deliveryAddress", "测试地址");
        body.put("items", Collections.singletonList(item));
        String key = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(key);
        io.restassured.RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .header("X-User-Id", 1L)
                .header("Idempotency-Key", key)
                .body(body)
                .when().post("/orders")
                .then().statusCode(200).body("code", equalTo(400));
    }
}
