package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.model.entity.OrderDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OrderMapper extends BaseMapper<OrderDO> {

    @Select("SELECT * FROM t_order WHERE order_no = #{orderNo} AND deleted = 0")
    OrderDO selectByOrderNo(@Param("orderNo") String orderNo);
}
