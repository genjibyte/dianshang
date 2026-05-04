package com.example.order.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("取消订单请求")
public class CancelOrderDTO {

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty(value = "订单号", required = true, example = "ORD20250801120000123456")
    private String orderNo;

    @NotBlank(message = "取消原因不能为空")
    @ApiModelProperty(value = "取消原因", required = true, example = "不想吃了")
    private String reason;
}
