package com.agenthub.domain.model.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能市场信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketInfo {
    private String marketId;
    private String marketName;
}
