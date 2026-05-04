package com.example.order.manager.impl;

import com.example.order.common.exception.BizException;
import com.example.order.common.response.ResponseCode;
import com.example.order.mapper.ProductMapper;
import com.example.order.model.entity.ProductDO;
import com.example.order.manager.StockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockManagerImpl implements StockManager {

    private final ProductMapper productMapper;

    @Override
    public ProductDO lockAndDeduct(Long productId, int quantity) {
        ProductDO product = productMapper.selectForUpdate(productId);
        if (product == null) {
            throw new BizException(ResponseCode.PRODUCT_NOT_FOUND, String.valueOf(productId));
        }
        if (product.getStatus() != 1) {
            throw new BizException(ResponseCode.PRODUCT_OFF_SHELF, product.getName());
        }
        if (product.getStock() < quantity) {
            throw new BizException(ResponseCode.STOCK_NOT_ENOUGH, product.getName());
        }

        int rows = productMapper.deductStock(productId, quantity);
        if (rows == 0) {
            throw new BizException(ResponseCode.STOCK_DEDUCT_FAIL, product.getName());
        }

        log.info("库存扣减成功: productId={}, quantity={}, remainStock={}",
                productId, quantity, product.getStock() - quantity);
        return product;
    }

    @Override
    public void restore(Long productId, int quantity) {
        int rows = productMapper.restoreStock(productId, quantity);
        if (rows > 0) {
            log.info("库存恢复成功: productId={}, quantity={}", productId, quantity);
        }
    }
}
