package com.example.order.controller;

import com.example.order.common.constant.OrderConstant;
import com.example.order.common.response.ApiResponse;
import com.example.order.model.bo.CreateOrderBO;
import com.example.order.model.dto.CancelOrderDTO;
import com.example.order.model.dto.CreateOrderDTO;
import com.example.order.model.vo.OrderVO;
import com.example.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping
    public ApiResponse<OrderVO> createOrder(
            @RequestHeader(OrderConstant.HEADER_USER_ID) Long userId,
            @RequestHeader(value = OrderConstant.HEADER_IDEMPOTENCY_KEY, required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderDTO dto) {

        CreateOrderBO bo = new CreateOrderBO();
        bo.setUserId(userId);
        bo.setShopId(dto.getShopId());
        bo.setDeliveryAddress(dto.getDeliveryAddress());
        bo.setRemark(dto.getRemark());
        bo.setIdempotencyKey(idempotencyKey);
        bo.setItems(dto.getItems().stream().map(item -> {
            CreateOrderBO.OrderItemBO itemBO = new CreateOrderBO.OrderItemBO();
            itemBO.setProductId(item.getProductId());
            itemBO.setQuantity(item.getQuantity());
            return itemBO;
        }).collect(Collectors.toList()));

        return ApiResponse.success(orderService.createOrder(bo));
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderVO> getOrder(
            @ApiParam("订单号") @PathVariable String orderNo) {
        return ApiResponse.success(orderService.getOrder(orderNo));
    }

    @ApiOperation("查询用户订单列表")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderVO>> listUserOrders(
            @ApiParam("用户ID") @PathVariable Long userId) {
        return ApiResponse.success(orderService.listUserOrders(userId));
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancel")
    public ApiResponse<OrderVO> cancelOrder(
            @RequestHeader(OrderConstant.HEADER_USER_ID) Long userId,
            @Valid @RequestBody CancelOrderDTO dto) {
        return ApiResponse.success(orderService.cancelOrder(userId, dto.getOrderNo(), dto.getReason()));
    }

    @ApiOperation("订单配送（商家/骑手调用）")
    @PostMapping("/{orderNo}/deliver")
    public ApiResponse<OrderVO> deliverOrder(
            @ApiParam("订单号") @PathVariable String orderNo) {
        return ApiResponse.success(orderService.deliverOrder(orderNo));
    }

    @ApiOperation("订单完成（骑手确认送达）")
    @PostMapping("/{orderNo}/complete")
    public ApiResponse<OrderVO> completeOrder(
            @ApiParam("订单号") @PathVariable String orderNo) {
        return ApiResponse.success(orderService.completeOrder(orderNo));
    }
}
