package com.agenthub.api.dto;

import com.agenthub.domain.model.TaskType;
import com.agenthub.domain.model.TierPlan;

import java.math.BigDecimal;

/**
 * 模型调用请求值对象。
 *
 * @param tenantId  租户标识，不能为空
 * @param taskType  任务类型
 * @param tierPlan  租户套餐等级
 * @param maxBudget 本次调用最大预算（美元），null 表示不限制
 * @param prompt    用户输入的提示文本
 */
public record ModelRequest(
        /** 租户标识，不能为空 */
        String tenantId,
        /** 任务类型 */
        TaskType taskType,
        /** 租户套餐等级 */
        TierPlan tierPlan,
        /** 本次调用最大预算（美元），null 表示不限制 */
        BigDecimal maxBudget,
        /** 用户输入的提示文本 */
        String prompt
) {
    /**
     * 创建不限预算的便捷构造。
     */
    public static ModelRequest of(String tenantId, TaskType taskType, TierPlan tierPlan, String prompt) {
        return new ModelRequest(tenantId, taskType, tierPlan, null, prompt);
    }
}
