package com.example.ordertest.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TestProductMapper {

    void resetStock(@Param("id") Long id, @Param("stock") Integer stock);

    Map<String, Object> selectById(@Param("id") Long id);
}
