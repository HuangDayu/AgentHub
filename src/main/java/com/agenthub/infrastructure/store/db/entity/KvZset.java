package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 有序集合存储实体
 */
@Data
@TableName("kv_zset")
public class KvZset {
    @TableId(type = IdType.INPUT)
    private String kvKey;
    private String member;
    private Double score;
}
