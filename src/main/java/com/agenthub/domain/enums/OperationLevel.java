package com.agenthub.domain.enums;

/**
 * 操作级别 - 细粒度 CRUD 4 个，去掉 ADMIN
 * <p>与传统 READ_ONLY/READ_WRITE/ADMIN 层级模型不同，本枚举表达
 * "允许的最小操作集合"，通过 Set 组合实现细粒度授权。</p>
 */
public enum OperationLevel {
    CREATE,
    READ,
    UPDATE,
    DELETE
}
