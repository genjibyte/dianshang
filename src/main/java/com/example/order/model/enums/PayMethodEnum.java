package com.example.order.model.enums;

import lombok.Getter;

@Getter
public enum PayMethodEnum {

    BALANCE(1, "余额"),
    WECHAT(2, "微信支付"),
    ALIPAY(3, "支付宝");

    private final int code;
    private final String desc;

    PayMethodEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayMethodEnum fromCode(int code) {
        for (PayMethodEnum method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知支付方式: " + code);
    }
}
