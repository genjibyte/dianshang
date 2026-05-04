package com.example.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.order.client.PaymentGatewayClient;
import com.example.order.client.PushNotificationClient;
import com.example.order.client.dto.GatewayPayRequest;
import com.example.order.client.dto.GatewayPayResponse;
import com.example.order.client.dto.PushMessageRequest;
import com.example.order.common.constant.OrderConstant;
import com.example.order.common.exception.BizException;
import com.example.order.common.response.ResponseCode;
import com.example.order.common.util.IdGenerator;
import com.example.order.manager.IdempotencyManager;
import com.example.order.mapper.OrderMapper;
import com.example.order.mapper.PaymentMapper;
import com.example.order.mapper.UserMapper;
import com.example.order.model.bo.PayOrderBO;
import com.example.order.model.entity.OrderDO;
import com.example.order.model.entity.PaymentDO;
import com.example.order.model.enums.OrderStatusEnum;
import com.example.order.model.enums.PayMethodEnum;
import com.example.order.model.enums.PayStatusEnum;
import com.example.order.model.vo.PaymentVO;
import com.example.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final IdempotencyManager idempotencyManager;
    private final PaymentGatewayClient paymentGatewayClient;
    private final PushNotificationClient pushNotificationClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO pay(PayOrderBO bo) {
        // 幂等校验
        if (bo.getIdempotencyKey() != null) {
            String existingPaymentNo = idempotencyManager.checkDuplicate(
                    bo.getIdempotencyKey(), OrderConstant.BIZ_TYPE_PAY_ORDER);
            if (existingPaymentNo != null) {
                log.info("支付幂等命中: paymentNo={}", existingPaymentNo);
                PaymentDO existing = paymentMapper.selectLatestByOrderNo(bo.getOrderNo());
                if (existing != null) {
                    return buildPaymentVO(existing);
                }
            }
        }

        // 校验订单
        OrderDO order = orderMapper.selectByOrderNo(bo.getOrderNo());
        if (order == null) {
            throw new BizException(ResponseCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(bo.getUserId())) {
            throw new BizException(ResponseCode.ORDER_NOT_BELONG_USER);
        }

        OrderStatusEnum orderStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!orderStatus.canPay()) {
            throw new BizException(ResponseCode.ORDER_STATUS_INVALID,
                    "当前状态[" + orderStatus.getDesc() + "]不允许支付");
        }

        PayMethodEnum payMethod = PayMethodEnum.fromCode(bo.getPayMethod());
        String paymentNo = IdGenerator.generatePaymentNo();
        String transactionId = "";

        // 余额支付直接扣减
        if (payMethod == PayMethodEnum.BALANCE) {
            int rows = userMapper.deductBalance(bo.getUserId(), order.getTotalAmount());
            if (rows == 0) {
                throw new BizException(ResponseCode.BALANCE_NOT_ENOUGH);
            }
            transactionId = "BAL_" + paymentNo;
        } else {
            // 微信/支付宝调用支付网关
            GatewayPayResponse gatewayResp = paymentGatewayClient.pay(GatewayPayRequest.builder()
                    .orderNo(bo.getOrderNo())
                    .amount(order.getTotalAmount())
                    .payMethod(bo.getPayMethod())
                    .notifyUrl("/api/payments/notify")
                    .build());

            if (gatewayResp == null || !gatewayResp.isSuccess()) {
                throw new BizException(ResponseCode.PAYMENT_GATEWAY_ERROR,
                        gatewayResp != null ? gatewayResp.getErrorMessage() : "网关无响应");
            }
            transactionId = gatewayResp.getTransactionId();
        }

        // 创建支付记录
        PaymentDO payment = new PaymentDO();
        payment.setPaymentNo(paymentNo);
        payment.setOrderNo(bo.getOrderNo());
        payment.setUserId(bo.getUserId());
        payment.setAmount(order.getTotalAmount());
        payment.setPayMethod(payMethod.getCode());
        payment.setStatus(PayStatusEnum.SUCCESS.getCode());
        payment.setTransactionId(transactionId);
        payment.setPaidTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        // 更新订单状态为已支付
        orderMapper.update(null, new LambdaUpdateWrapper<OrderDO>()
                .eq(OrderDO::getOrderNo, bo.getOrderNo())
                .eq(OrderDO::getVersion, order.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.PAID.getCode())
                .set(OrderDO::getPaidTime, LocalDateTime.now())
                .set(OrderDO::getVersion, order.getVersion() + 1));

        // 保存幂等记录
        if (bo.getIdempotencyKey() != null) {
            idempotencyManager.saveResult(bo.getIdempotencyKey(), OrderConstant.BIZ_TYPE_PAY_ORDER, paymentNo);
        }

        log.info("支付成功: paymentNo={}, orderNo={}, amount={}, method={}",
                paymentNo, bo.getOrderNo(), order.getTotalAmount(), payMethod.getDesc());

        pushNotificationClient.push(PushMessageRequest.builder()
                .userId(bo.getUserId())
                .title("支付成功")
                .content("订单 " + bo.getOrderNo() + " 支付成功，商家正在备餐")
                .bizType("PAYMENT_SUCCESS")
                .bizId(paymentNo)
                .build());

        return buildPaymentVO(payment);
    }

    @Override
    public PaymentVO getPaymentByOrderNo(String orderNo) {
        PaymentDO payment = paymentMapper.selectLatestByOrderNo(orderNo);
        if (payment == null) {
            throw new BizException(ResponseCode.PAYMENT_NOT_FOUND);
        }
        return buildPaymentVO(payment);
    }

    private PaymentVO buildPaymentVO(PaymentDO payment) {
        PaymentVO vo = new PaymentVO();
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setAmount(payment.getAmount());
        vo.setPayMethod(payment.getPayMethod());
        vo.setPayMethodDesc(PayMethodEnum.fromCode(payment.getPayMethod()).getDesc());
        vo.setStatus(payment.getStatus());
        vo.setStatusDesc(PayStatusEnum.fromCode(payment.getStatus()).getDesc());
        vo.setTransactionId(payment.getTransactionId());
        vo.setPaidTime(payment.getPaidTime());
        return vo;
    }
}
