package com.example.order.manager;

import com.example.order.model.entity.ProductDO;

public interface StockManager {

    /**
     * 锁定并扣减库存（SELECT FOR UPDATE + UPDATE）
     */
    ProductDO lockAndDeduct(Long productId, int quantity);

    /**
     * 恢复库存（取消订单时调用）
     */
    void restore(Long productId, int quantity);
}
