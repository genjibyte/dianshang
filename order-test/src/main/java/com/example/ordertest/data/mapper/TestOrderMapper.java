package com.example.ordertest.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TestOrderMapper {

    void deleteItemsByOrderNo(@Param("orderNo") String orderNo);

    void deleteByOrderNo(@Param("orderNo") String orderNo);

    Map<String, Object> selectByOrderNo(@Param("orderNo") String orderNo);

    int countByUserId(@Param("userId") Long userId);
}
