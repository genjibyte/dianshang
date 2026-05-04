package com.example.order.controller;

import com.example.order.common.constant.OrderConstant;
import com.example.order.common.response.ApiResponse;
import com.example.order.model.bo.PayOrderBO;
import com.example.order.model.dto.PayOrderDTO;
import com.example.order.model.vo.PaymentVO;
import com.example.order.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "支付接口")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @ApiOperation("支付订单")
    @PostMapping
    public ApiResponse<PaymentVO> pay(
            @RequestHeader(OrderConstant.HEADER_USER_ID) Long userId,
            @RequestHeader(value = OrderConstant.HEADER_IDEMPOTENCY_KEY, required = false) String idempotencyKey,
            @Valid @RequestBody PayOrderDTO dto) {

        PayOrderBO bo = new PayOrderBO();
        bo.setUserId(userId);
        bo.setOrderNo(dto.getOrderNo());
        bo.setPayMethod(dto.getPayMethod());
        bo.setIdempotencyKey(idempotencyKey);

        return ApiResponse.success(paymentService.pay(bo));
    }

    @ApiOperation("查询订单支付信息")
    @GetMapping("/order/{orderNo}")
    public ApiResponse<PaymentVO> getPaymentByOrderNo(
            @ApiParam("订单号") @PathVariable String orderNo) {
        return ApiResponse.success(paymentService.getPaymentByOrderNo(orderNo));
    }
}
