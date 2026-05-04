package com.example.order.model.bo;

import lombok.Data;

@Data
public class PayOrderBO {

    private Long userId;
    private String orderNo;
    private Integer payMethod;
    private String idempotencyKey;
}
