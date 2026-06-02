package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 列表存储实体
 */
@Data
@TableName("kv_list")
public class KvList {
    @TableId(type = IdType.INPUT)
    private String kvKey;
    private Long listIndex;
    private String kvValue;
}
