package com.example.ordertest.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TestIdempotencyMapper {

    void deleteByKey(@Param("key") String key, @Param("bizType") String bizType);

    void deleteByBizId(@Param("bizId") String bizId);
}
