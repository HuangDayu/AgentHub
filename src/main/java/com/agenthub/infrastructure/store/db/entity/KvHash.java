package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 哈希存储实体
 */
@Data
@TableName("kv_hash")
public class KvHash {
    @TableId(type = IdType.INPUT)
    private String kvKey;
    private String field;
    private String kvValue;
}
