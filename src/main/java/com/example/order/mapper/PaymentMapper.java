package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.model.entity.PaymentDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PaymentMapper extends BaseMapper<PaymentDO> {

    @Select("SELECT * FROM t_payment WHERE order_no = #{orderNo} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    PaymentDO selectLatestByOrderNo(@Param("orderNo") String orderNo);
}
