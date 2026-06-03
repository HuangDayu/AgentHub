package com.agenthub.application.port.out.skills;

import com.agenthub.domain.model.skill.MarketSearchQuery;
import com.agenthub.domain.model.skill.MarketSkillDetail;
import com.agenthub.domain.model.skill.MarketSkillSummary;
import java.util.List;

/**
 * 技能市场端口，定义市场搜索能力。
 */
public interface SkillMarketPort {

    /**
     * 市场唯一标识。
     */
    String getMarketId();

    /**
     * 市场显示名称。
     */
    String getMarketName();

    /**
     * 是否可用。
     */
    boolean isAvailable();

    /**
     * 搜索技能。
     */
    List<MarketSkillSummary> search(MarketSearchQuery query);

    /**
     * 获取技能详情。
     */
    MarketSkillDetail getDetail(String skillId);
}
