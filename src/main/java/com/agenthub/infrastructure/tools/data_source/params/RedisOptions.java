package com.agenthub.infrastructure.tools.data_source.params;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis 操作选项 DTO
 */
@Data
@NoArgsConstructor
public class RedisOptions {

    /** 值（SET/HSET等写命令使用） */
    private String value;

    /** 额外参数JSON数组 */
    private String args;
}
