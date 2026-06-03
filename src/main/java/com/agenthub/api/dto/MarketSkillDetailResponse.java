package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 市场技能详情响应。
 */
@Data
@NoArgsConstructor
public class MarketSkillDetailResponse {
    private String marketId;
    private String skillId;
    private String skillCode;
    private String name;
    private String description;
    private String author;
    private String version;
    private String license;
    private String homepage;
    private String downloadUrl;
    private List<String> tags;
    private int downloadCount;
    private int starCount;
    private Instant updatedAt;
    private String readmeContent;
}
