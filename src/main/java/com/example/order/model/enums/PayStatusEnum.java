package com.example.order.model.enums;

import lombok.Getter;

@Getter
public enum PayStatusEnum {

    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String desc;

    PayStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayStatusEnum fromCode(int code) {
        for (PayStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知支付状态码: " + code);
    }
}
