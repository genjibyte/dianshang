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
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("message", equalTo("success"))
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
    @DisplayName("多商品创建订单 - 验证总金额计算")
    public void testCreateOrderWithMultipleItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(buildItem(1L, 2));  // 宫保鸡丁 28 * 2 = 56
        items.add(buildItem(2L, 3));  // 鱼香肉丝 25 * 3 = 75
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        Response response = orderApiClient.createOrder(
                1L, 1L, "北京市海淀区中关村大街1号", "多放醋", items, idempotencyKey);

        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }

        response.then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.orderNo", notNullValue())
                .body("data.status", equalTo(0))
                .body("data.items", hasSize(2))
                .body("data.totalAmount", equalTo(131.0F));
    }

    @Test
    @Story("店铺已关闭")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("店铺已关闭时创建订单 - 期望返回错误码1003")
    public void testCreateOrderShopClosed() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        Response response = orderApiClient.createOrder(
                1L, 2L, "测试地址", "备注", items, idempotencyKey);

        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }

        response.then()
                .statusCode(200)
                .body("code", equalTo(1003));
    }

    @Test
    @Story("商品已下架")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("商品已下架时创建订单 - 期望返回错误码1005")
    public void testCreateOrderProductOffShelf() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(5L, 1));
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        Response response = orderApiClient.createOrder(
                1L, 1L, "测试地址", "备注", items, idempotencyKey);

        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }

        response.then()
                .statusCode(200)
                .body("code", equalTo(1005));
    }

    @Test
    @Story("库存不足")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("库存不足时创建订单 - 期望返回错误码1007")
    public void testCreateOrderStockNotEnough() {
        // productId=4 has stock=0 in the system
        List<Map<String, Object>> items = Collections.singletonList(buildItem(4L, 1));
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        Response response = orderApiClient.createOrder(
                1L, 1L, "测试地址", "备注", items, idempotencyKey);

        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }

        response.then()
                .statusCode(200)
                .body("code", equalTo(1007));
    }

    @Test
    @Story("参数校验失败")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("缺少必填字段时创建订单 - 期望返回错误码400")
    public void testCreateOrderInvalidParams() {
        // Missing items
        Map<String, Object> body = new HashMap<>();
        body.put("shopId", 1);
        body.put("deliveryAddress", "测试地址");

        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        Response response = io.restassured.RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .header("X-User-Id", 1L)
                .header("Idempotency-Key", idempotencyKey)
                .body(body)
                .when()
                .post("/orders");

        response.then()
                .statusCode(200)
                .body("code", equalTo(400));
    }

    @Test
    @Story("用户不存在")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("用户不存在时创建订单 - 期望返回错误码1001")
    public void testCreateOrderUserNotFound() {
        List<Map<String, Object>> items = Collections.singletonList(buildItem(1L, 1));
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        Response response = orderApiClient.createOrder(
                999L, 1L, "测试地址", "备注", items, idempotencyKey);

        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }

        response.then()
                .statusCode(200)
                .body("code", equalTo(1001));
    }

    @Test
    @Story("商品不属于该店铺")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("商品不属于该店铺时创建订单 - 期望返回错误码1006")
    public void testCreateOrderProductNotInShop() {
        // productId=6 belongs to shop2, but we order from shop1
        List<Map<String, Object>> items = Collections.singletonList(buildItem(6L, 1));
        String idempotencyKey = UUID.randomUUID().toString();
        usedIdempotencyKeys.add(idempotencyKey);

        Response response = orderApiClient.createOrder(
                1L, 1L, "测试地址", "备注", items, idempotencyKey);

        String orderNo = response.jsonPath().getString("data.orderNo");
        if (orderNo != null) {
            createdOrderNos.add(orderNo);
        }

        response.then()
                .statusCode(200)
                .body("code", equalTo(1006));
    }
}
