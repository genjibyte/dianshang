package com.example.order.model.bo;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderBO {

    private Long userId;
    private Long shopId;
    private String deliveryAddress;
    private String remark;
    private String idempotencyKey;
    private List<OrderItemBO> items;

    @Data
    public static class OrderItemBO {
        private Long productId;
        private Integer quantity;
    }
}
