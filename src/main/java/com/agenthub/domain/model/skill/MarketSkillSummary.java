package com.agenthub.domain.model.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * 市场技能搜索结果摘要。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSkillSummary {
    private String marketId;
    private String skillId;
    private String skillCode;
    private String name;
    private String description;
    private String author;
    private String version;
    private int downloadCount;
    private int starCount;
    private String thumbnailUrl;
    private Instant updatedAt;
}
