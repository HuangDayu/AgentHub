package com.agenthub.domain.enums;

/**
 * 租户套餐等级枚举。
 * <p>
 * 不同套餐可使用的模型范围不同：
 * <ul>
 *   <li>BASIC — 仅限低成本模型</li>
 *   <li>STANDARD — 包含主流模型</li>
 *   <li>PREMIUM — 全量模型含旗舰模型</li>
 * </ul>
 */
public enum TierPlan {

    /** 仅限低成本模型 */
    BASIC,

    /** 包含主流模型 */
    STANDARD,

    /** 全量模型含旗舰模型 */
    PREMIUM
}
