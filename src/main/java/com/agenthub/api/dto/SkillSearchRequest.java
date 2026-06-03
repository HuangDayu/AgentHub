package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 技能搜索请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillSearchRequest {
    private String keyword;
}
