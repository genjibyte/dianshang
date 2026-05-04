package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.model.entity.UserDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

public interface UserMapper extends BaseMapper<UserDO> {

    @Update("UPDATE t_user SET balance = balance - #{amount}, version = version + 1, " +
            "update_time = CURRENT_TIMESTAMP WHERE id = #{userId} AND balance >= #{amount} AND deleted = 0")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Update("UPDATE t_user SET balance = balance + #{amount}, version = version + 1, " +
            "update_time = CURRENT_TIMESTAMP WHERE id = #{userId} AND deleted = 0")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
