package com.example.order.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_idempotency")
public class IdempotencyDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String idempotencyKey;

    private String bizType;

    private String bizId;

    private LocalDateTime createTime;
}
