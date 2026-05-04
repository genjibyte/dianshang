package com.example.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.order.client.PaymentGatewayClient;
import com.example.order.client.PushNotificationClient;
import com.example.order.client.dto.GatewayRefundRequest;
import com.example.order.client.dto.GatewayRefundResponse;
import com.example.order.client.dto.PushMessageRequest;
import com.example.order.common.constant.OrderConstant;
import com.example.order.common.exception.BizException;
import com.example.order.common.response.ResponseCode;
import com.example.order.common.util.IdGenerator;
import com.example.order.manager.IdempotencyManager;
import com.example.order.manager.StockManager;
import com.example.order.mapper.*;
import com.example.order.model.bo.CreateOrderBO;
import com.example.order.model.entity.*;
import com.example.order.model.enums.OrderStatusEnum;
import com.example.order.model.enums.PayStatusEnum;
import com.example.order.model.vo.OrderItemVO;
import com.example.order.model.vo.OrderVO;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final ShopMapper shopMapper;
    private final ProductMapper productMapper;
    private final PaymentMapper paymentMapper;
    private final IdempotencyManager idempotencyManager;
    private final StockManager stockManager;
    private final PaymentGatewayClient paymentGatewayClient;
    private final PushNotificationClient pushNotificationClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderBO bo) {
        // 幂等校验
        if (bo.getIdempotencyKey() != null) {
            String existingOrderNo = idempotencyManager.checkDuplicate(
                    bo.getIdempotencyKey(), OrderConstant.BIZ_TYPE_CREATE_ORDER);
            if (existingOrderNo != null) {
                log.info("幂等命中，返回已有订单: orderNo={}", existingOrderNo);
                return getOrder(existingOrderNo);
            }
        }

        // 校验用户
        UserDO user = userMapper.selectById(bo.getUserId());
        if (user == null) {
            throw new BizException(ResponseCode.USER_NOT_FOUND);
        }

        // 校验店铺
        ShopDO shop = shopMapper.selectById(bo.getShopId());
        if (shop == null) {
            throw new BizException(ResponseCode.SHOP_NOT_FOUND);
        }
        if (shop.getStatus() != 1) {
            throw new BizException(ResponseCode.SHOP_CLOSED);
        }

        // 锁定库存 + 计算金额
        String orderNo = IdGenerator.generateOrderNo();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemDO> orderItems = new ArrayList<>();

        for (CreateOrderBO.OrderItemBO itemBO : bo.getItems()) {
            ProductDO product = stockManager.lockAndDeduct(itemBO.getProductId(), itemBO.getQuantity());

            if (!product.getShopId().equals(bo.getShopId())) {
                throw new BizException(ResponseCode.PRODUCT_NOT_IN_SHOP, product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemBO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItemDO orderItem = new OrderItemDO();
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(itemBO.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
        }

        // 创建订单
        OrderDO order = new OrderDO();
        order.setOrderNo(orderNo);
        order.setUserId(bo.getUserId());
        order.setShopId(bo.getShopId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setDeliveryAddress(bo.getDeliveryAddress());
        order.setRemark(bo.getRemark() != null ? bo.getRemark() : "");
        orderMapper.insert(order);

        // 批量插入订单明细
        for (OrderItemDO item : orderItems) {
            orderItemMapper.insert(item);
        }

        // 保存幂等记录
        if (bo.getIdempotencyKey() != null) {
            idempotencyManager.saveResult(bo.getIdempotencyKey(), OrderConstant.BIZ_TYPE_CREATE_ORDER, orderNo);
        }

        log.info("订单创建成功: orderNo={}, userId={}, totalAmount={}", orderNo, bo.getUserId(), totalAmount);

        // 异步推送通知（不影响主流程）
        pushNotificationClient.push(PushMessageRequest.builder()
                .userId(bo.getUserId())
                .title("下单成功")
                .content("订单 " + orderNo + " 已创建，请在15分钟内完成支付")
                .bizType("ORDER_CREATED")
                .bizId(orderNo)
                .build());

        return buildOrderVO(order, orderItems, shop.getName());
    }

    @Override
    public OrderVO getOrder(String orderNo) {
        OrderDO order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ResponseCode.ORDER_NOT_FOUND);
        }
        List<OrderItemDO> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderNo, orderNo));
        ShopDO shop = shopMapper.selectById(order.getShopId());
        return buildOrderVO(order, items, shop != null ? shop.getName() : "");
    }

    @Override
    public List<OrderVO> listUserOrders(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResponseCode.USER_NOT_FOUND);
        }

        List<OrderDO> orders = orderMapper.selectList(
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getUserId, userId)
                        .orderByDesc(OrderDO::getCreateTime));

        return orders.stream().map(order -> {
            List<OrderItemDO> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderNo, order.getOrderNo()));
            ShopDO shop = shopMapper.selectById(order.getShopId());
            return buildOrderVO(order, items, shop != null ? shop.getName() : "");
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancelOrder(Long userId, String orderNo, String reason) {
        OrderDO order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ResponseCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(ResponseCode.ORDER_NOT_BELONG_USER);
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!currentStatus.canCancel()) {
            throw new BizException(ResponseCode.ORDER_STATUS_INVALID,
                    "当前状态[" + currentStatus.getDesc() + "]不允许取消");
        }

        // CAS 更新状态
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<OrderDO>()
                .eq(OrderDO::getOrderNo, orderNo)
                .eq(OrderDO::getStatus, order.getStatus())
                .eq(OrderDO::getVersion, order.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.CANCELLED.getCode())
                .set(OrderDO::getCancelReason, reason)
                .set(OrderDO::getCancelledTime, LocalDateTime.now())
                .set(OrderDO::getVersion, order.getVersion() + 1));

        if (rows == 0) {
            throw new BizException(ResponseCode.ORDER_CANCEL_FAIL);
        }

        // 恢复库存
        List<OrderItemDO> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderNo, orderNo));
        for (OrderItemDO item : items) {
            stockManager.restore(item.getProductId(), item.getQuantity());
        }

        // 已支付订单需退款
        if (currentStatus == OrderStatusEnum.PAID) {
            PaymentDO payment = paymentMapper.selectLatestByOrderNo(orderNo);
            if (payment != null && payment.getStatus() == PayStatusEnum.SUCCESS.getCode()) {
                processRefund(order, payment, reason);
            }
        }

        log.info("订单取消成功: orderNo={}, reason={}", orderNo, reason);

        pushNotificationClient.push(PushMessageRequest.builder()
                .userId(userId)
                .title("订单已取消")
                .content("订单 " + orderNo + " 已取消，原因: " + reason)
                .bizType("ORDER_CANCELLED")
                .bizId(orderNo)
                .build());

        return getOrder(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO deliverOrder(String orderNo) {
        OrderDO order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ResponseCode.ORDER_NOT_FOUND);
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!currentStatus.canDeliver()) {
            throw new BizException(ResponseCode.ORDER_STATUS_INVALID,
                    "当前状态[" + currentStatus.getDesc() + "]不允许配送");
        }

        orderMapper.update(null, new LambdaUpdateWrapper<OrderDO>()
                .eq(OrderDO::getOrderNo, orderNo)
                .eq(OrderDO::getVersion, order.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.DELIVERING.getCode())
                .set(OrderDO::getDeliveredTime, LocalDateTime.now())
                .set(OrderDO::getVersion, order.getVersion() + 1));

        pushNotificationClient.push(PushMessageRequest.builder()
                .userId(order.getUserId())
                .title("订单配送中")
                .content("订单 " + orderNo + " 骑手已取餐，正在配送")
                .bizType("ORDER_DELIVERING")
                .bizId(orderNo)
                .build());

        return getOrder(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO completeOrder(String orderNo) {
        OrderDO order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(ResponseCode.ORDER_NOT_FOUND);
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!currentStatus.canComplete()) {
            throw new BizException(ResponseCode.ORDER_STATUS_INVALID,
                    "当前状态[" + currentStatus.getDesc() + "]不允许完成");
        }

        orderMapper.update(null, new LambdaUpdateWrapper<OrderDO>()
                .eq(OrderDO::getOrderNo, orderNo)
                .eq(OrderDO::getVersion, order.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.COMPLETED.getCode())
                .set(OrderDO::getCompletedTime, LocalDateTime.now())
                .set(OrderDO::getVersion, order.getVersion() + 1));

        pushNotificationClient.push(PushMessageRequest.builder()
                .userId(order.getUserId())
                .title("订单已完成")
                .content("订单 " + orderNo + " 已完成，感谢使用")
                .bizType("ORDER_COMPLETED")
                .bizId(orderNo)
                .build());

        return getOrder(orderNo);
    }

    private void processRefund(OrderDO order, PaymentDO payment, String reason) {
        // 调用支付网关退款
        GatewayRefundResponse refundResp = paymentGatewayClient.refund(GatewayRefundRequest.builder()
                .orderNo(order.getOrderNo())
                .transactionId(payment.getTransactionId())
                .refundAmount(payment.getAmount())
                .reason(reason)
                .build());

        if (refundResp != null && refundResp.isSuccess()) {
            paymentMapper.update(null, new LambdaUpdateWrapper<PaymentDO>()
                    .eq(PaymentDO::getPaymentNo, payment.getPaymentNo())
                    .set(PaymentDO::getStatus, PayStatusEnum.REFUNDED.getCode()));
            // 余额支付退回余额
            userMapper.addBalance(order.getUserId(), payment.getAmount());
            log.info("退款成功: orderNo={}, amount={}", order.getOrderNo(), payment.getAmount());
        } else {
            log.error("退款失败: orderNo={}", order.getOrderNo());
            throw new BizException(ResponseCode.REFUND_FAIL);
        }
    }

    private OrderVO buildOrderVO(OrderDO order, List<OrderItemDO> items, String shopName) {
        OrderVO vo = new OrderVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setShopId(order.getShopId());
        vo.setShopName(shopName);
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(OrderStatusEnum.fromCode(order.getStatus()).getDesc());
        vo.setDeliveryAddress(order.getDeliveryAddress());
        vo.setRemark(order.getRemark());
        vo.setCancelReason(order.getCancelReason());
        vo.setPaidTime(order.getPaidTime());
        vo.setDeliveredTime(order.getDeliveredTime());
        vo.setCompletedTime(order.getCompletedTime());
        vo.setCancelledTime(order.getCancelledTime());
        vo.setCreateTime(order.getCreateTime());

        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotal(item.getSubtotal());
            return itemVO;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);

        return vo;
    }
}
