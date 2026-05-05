package com.example.order.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class OrderDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long shopId;

    private BigDecimal totalAmount;

    private Integer status;

    private String deliveryAddress;

    private String remark;

    private String cancelReason;

    private LocalDateTime paidTime;

    private LocalDateTime deliveredTime;

    private LocalDateTime completedTime;

    private LocalDateTime cancelledTime;

    private Integer version;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
