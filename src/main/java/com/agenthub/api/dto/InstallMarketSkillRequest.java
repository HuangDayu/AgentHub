package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从市场安装技能请求。
 */
@Data
@NoArgsConstructor
public class InstallMarketSkillRequest {
    private String marketId;
    private String skillId;
}
