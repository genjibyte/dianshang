package com.example.order.manager;

public interface IdempotencyManager {

    /**
     * 检查是否为重复请求，如果是则返回关联的业务ID
     * @return 业务ID（非空表示重复请求），null 表示首次请求
     */
    String checkDuplicate(String idempotencyKey, String bizType);

    /**
     * 记录幂等结果
     */
    void saveResult(String idempotencyKey, String bizType, String bizId);
}
