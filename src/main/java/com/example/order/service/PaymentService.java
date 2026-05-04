package com.example.order.service;

import com.example.order.model.bo.PayOrderBO;
import com.example.order.model.vo.PaymentVO;

public interface PaymentService {

    PaymentVO pay(PayOrderBO bo);

    PaymentVO getPaymentByOrderNo(String orderNo);
}
