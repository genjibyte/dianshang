package com.example.order.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("支付信息响应")
public class PaymentVO {

    @ApiModelProperty("支付单号")
    private String paymentNo;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("支付金额")
    private BigDecimal amount;

    @ApiModelProperty("支付方式: 1-余额 2-微信 3-支付宝")
    private Integer payMethod;

    @ApiModelProperty("支付方式描述")
    private String payMethodDesc;

    @ApiModelProperty("支付状态: 0-待支付 1-成功 2-失败 3-已退款")
    private Integer status;

    @ApiModelProperty("支付状态描述")
    private String statusDesc;

    @ApiModelProperty("第三方交易流水号")
    private String transactionId;

    @ApiModelProperty("支付时间")
    private LocalDateTime paidTime;
}
