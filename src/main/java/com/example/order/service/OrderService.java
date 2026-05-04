package com.example.order.service;

import com.example.order.model.bo.CreateOrderBO;
import com.example.order.model.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderVO createOrder(CreateOrderBO bo);

    OrderVO getOrder(String orderNo);

    List<OrderVO> listUserOrders(Long userId);

    OrderVO cancelOrder(Long userId, String orderNo, String reason);

    OrderVO deliverOrder(String orderNo);

    OrderVO completeOrder(String orderNo);
}
