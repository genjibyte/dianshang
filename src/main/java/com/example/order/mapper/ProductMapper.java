package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.model.entity.ProductDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<ProductDO> {

    @Select("SELECT * FROM t_product WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    ProductDO selectForUpdate(@Param("id") Long id);

    @Update("UPDATE t_product SET stock = stock - #{quantity}, version = version + 1, " +
            "update_time = CURRENT_TIMESTAMP WHERE id = #{id} AND stock >= #{quantity} AND deleted = 0")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("UPDATE t_product SET stock = stock + #{quantity}, version = version + 1, " +
            "update_time = CURRENT_TIMESTAMP WHERE id = #{id} AND deleted = 0")
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);
}
