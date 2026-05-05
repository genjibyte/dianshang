package com.example.ordertest.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TestPaymentMapper {

    void deleteByOrderNo(@Param("orderNo") String orderNo);

    Map<String, Object> selectByOrderNo(@Param("orderNo") String orderNo);
}
