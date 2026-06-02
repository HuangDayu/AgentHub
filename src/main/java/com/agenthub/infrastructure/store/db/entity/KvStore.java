package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 键值存储实体
 */
@Data
@TableName("kv_store")
public class KvStore {
    @TableId(type = IdType.INPUT)
    private String kvKey;
    private String kvValue;
    private String kvType;
    private Long expireTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
