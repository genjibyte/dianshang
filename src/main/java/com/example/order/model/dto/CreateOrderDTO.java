package com.example.order.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("创建订单请求")
public class CreateOrderDTO {

    @NotNull(message = "店铺ID不能为空")
    @ApiModelProperty(value = "店铺ID", required = true, example = "1")
    private Long shopId;

    @NotBlank(message = "收货地址不能为空")
    @ApiModelProperty(value = "收货地址", required = true, example = "北京市朝阳区望京SOHO")
    private String deliveryAddress;

    @ApiModelProperty(value = "订单备注", example = "少放辣")
    private String remark;

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    @ApiModelProperty(value = "商品列表", required = true)
    private List<OrderItemDTO> items;
}
