package com.example.order.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("支付订单请求")
public class PayOrderDTO {

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty(value = "订单号", required = true, example = "ORD20250801120000123456")
    private String orderNo;

    @NotNull(message = "支付方式不能为空")
    @ApiModelProperty(value = "支付方式: 1-余额 2-微信 3-支付宝", required = true, example = "1")
    private Integer payMethod;
}
