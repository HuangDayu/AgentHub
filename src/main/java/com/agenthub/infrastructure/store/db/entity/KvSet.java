package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 集合存储实体
 */
@Data
@TableName("kv_set")
public class KvSet {
    @TableId(type = IdType.INPUT)
    private String kvKey;
    private String member;
}
