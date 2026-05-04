package com.example.order.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("订单详情响应")
public class OrderVO {

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("店铺ID")
    private Long shopId;

    @ApiModelProperty("店铺名称")
    private String shopName;

    @ApiModelProperty("订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("订单状态码")
    private Integer status;

    @ApiModelProperty("订单状态描述")
    private String statusDesc;

    @ApiModelProperty("收货地址")
    private String deliveryAddress;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("取消原因")
    private String cancelReason;

    @ApiModelProperty("支付时间")
    private LocalDateTime paidTime;

    @ApiModelProperty("配送时间")
    private LocalDateTime deliveredTime;

    @ApiModelProperty("完成时间")
    private LocalDateTime completedTime;

    @ApiModelProperty("取消时间")
    private LocalDateTime cancelledTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("订单明细")
    private List<OrderItemVO> items;
}
