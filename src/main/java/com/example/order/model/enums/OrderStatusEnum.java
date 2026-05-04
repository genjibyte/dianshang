package com.example.order.model.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付"),
    DELIVERING(2, "配送中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态码: " + code);
    }

    public boolean canCancel() {
        return this == PENDING_PAYMENT || this == PAID;
    }

    public boolean canPay() {
        return this == PENDING_PAYMENT;
    }

    public boolean canDeliver() {
        return this == PAID;
    }

    public boolean canComplete() {
        return this == DELIVERING;
    }
}
