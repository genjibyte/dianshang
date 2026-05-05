package com.example.ordertest.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface TestUserMapper {

    void resetBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

    Map<String, Object> selectById(@Param("id") Long id);
}
