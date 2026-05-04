package com.example.order.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.order.mapper.IdempotencyMapper;
import com.example.order.model.entity.IdempotencyDO;
import com.example.order.manager.IdempotencyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyManagerImpl implements IdempotencyManager {

    private final IdempotencyMapper idempotencyMapper;

    @Override
    public String checkDuplicate(String idempotencyKey, String bizType) {
        IdempotencyDO record = idempotencyMapper.selectOne(
                new LambdaQueryWrapper<IdempotencyDO>()
                        .eq(IdempotencyDO::getIdempotencyKey, idempotencyKey)
                        .eq(IdempotencyDO::getBizType, bizType)
        );
        return record != null ? record.getBizId() : null;
    }

    @Override
    public void saveResult(String idempotencyKey, String bizType, String bizId) {
        IdempotencyDO record = new IdempotencyDO();
        record.setIdempotencyKey(idempotencyKey);
        record.setBizType(bizType);
        record.setBizId(bizId);
        try {
            idempotencyMapper.insert(record);
        } catch (DuplicateKeyException e) {
            log.warn("幂等记录已存在: key={}, bizType={}", idempotencyKey, bizType);
        }
    }
}
