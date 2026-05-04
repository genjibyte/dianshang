package com.example.order.common.response;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // 业务错误码 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    SHOP_NOT_FOUND(1002, "店铺不存在"),
    SHOP_CLOSED(1003, "店铺已休息，暂停接单"),
    PRODUCT_NOT_FOUND(1004, "商品不存在"),
    PRODUCT_OFF_SHELF(1005, "商品已下架"),
    PRODUCT_NOT_IN_SHOP(1006, "商品不属于该店铺"),
    STOCK_NOT_ENOUGH(1007, "库存不足"),
    STOCK_DEDUCT_FAIL(1008, "库存扣减失败，请重试"),

    // 订单错误码 2xxx
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_INVALID(2002, "订单状态不允许当前操作"),
    ORDER_NOT_BELONG_USER(2003, "无权操作此订单"),
    ORDER_CANCEL_FAIL(2004, "取消订单失败"),
    ORDER_ALREADY_CANCELLED(2005, "订单已取消"),

    // 支付错误码 3xxx
    PAYMENT_NOT_FOUND(3001, "支付记录不存在"),
    BALANCE_NOT_ENOUGH(3002, "余额不足"),
    BALANCE_DEDUCT_FAIL(3003, "余额扣减失败"),
    PAYMENT_GATEWAY_ERROR(3004, "支付网关调用失败"),
    PAY_STATUS_INVALID(3005, "支付状态异常"),
    REFUND_FAIL(3006, "退款失败"),

    // 幂等错误码 4xxx
    IDEMPOTENCY_KEY_MISSING(4001, "幂等键缺失"),
    DUPLICATE_REQUEST(4002, "重复请求");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
