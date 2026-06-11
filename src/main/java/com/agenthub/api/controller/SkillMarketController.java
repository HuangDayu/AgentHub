package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.InstallMarketSkillRequest;
import com.agenthub.api.dto.MarketInfoResponse;
import com.agenthub.api.dto.MarketSearchRequest;
import com.agenthub.api.dto.MarketSkillDetailResponse;
import com.agenthub.api.dto.MarketSkillSummaryResponse;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.usecase.SkillMarketUseCase;
import com.agenthub.application.dto.MarketSearchQuery;
import com.agenthub.infrastructure.context.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 技能市场控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skills/market")
@RequiredArgsConstructor
public class SkillMarketController {

    private final SkillMarketUseCase skillMarketUseCase;

    /**
     * 获取所有可用市场列表。
     */
    @GetMapping("/list")
    public ResponseEntity<List<MarketInfoResponse>> listMarkets(
            @PathVariable String workspaceId) {
        List<Map<String, String>> markets = skillMarketUseCase.listMarkets();
        List<MarketInfoResponse> response = markets.stream()
                .map(this::toMarketInfoResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 转换市场信息。
     */
    private MarketInfoResponse toMarketInfoResponse(Map<String, String> map) {
        MarketInfoResponse response = new MarketInfoResponse();
        response.setMarketId(map.get("marketId"));
        response.setMarketName(map.get("marketName"));
        return response;
    }

    /**
     * 并行搜索所有市场。
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, List<MarketSkillSummaryResponse>>> searchAll(
            @PathVariable String workspaceId,
            @RequestBody MarketSearchRequest request) {
        MarketSearchQuery query = new MarketSearchQuery(
                request.getKeyword(), request.getCategory(),
                request.getSortBy(), request.getPage(), request.getPageSize());
        Map<String, List<Map<String, Object>>> raw = skillMarketUseCase.searchAll(query);
        Map<String, List<MarketSkillSummaryResponse>> response = raw.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(this::toSummaryResponse)
                                .collect(Collectors.toList())));
        return ResponseEntity.ok(response);
    }

    /**
     * 将 Map 转换为搜索结果响应。
     */
    private MarketSkillSummaryResponse toSummaryResponse(Map<String, Object> m) {
        MarketSkillSummaryResponse response = new MarketSkillSummaryResponse();
        populateSummary(response, m);
        populateSummaryCounts(response, m);
        return response;
    }

    /** 填充文本字段。 */
    private void populateSummary(MarketSkillSummaryResponse response, Map<String, Object> m) {
        response.setMarketId((String) m.get("marketId"));
        response.setSkillId((String) m.get("skillId"));
        response.setSkillCode((String) m.get("skillCode"));
        response.setName((String) m.get("name"));
        response.setDescription((String) m.get("description"));
        response.setAuthor((String) m.get("author"));
        response.setVersion((String) m.get("version"));
    }

    /** 填充数值字段。 */
    private void populateSummaryCounts(MarketSkillSummaryResponse response, Map<String, Object> m) {
        response.setDownloadCount((int) m.get("downloadCount"));
        response.setStarCount((int) m.get("starCount"));
        response.setThumbnailUrl((String) m.get("thumbnailUrl"));
    }

    /**
     * 获取市场技能详情。
     */
    @GetMapping("/detail")
    public ResponseEntity<MarketSkillDetailResponse> getDetail(
            @PathVariable String workspaceId,
            @RequestParam String marketId,
            @RequestParam String skillId) {
        return fetchDetail(marketId, skillId);
    }

    /** 查询详情并处理异常。 */
    private ResponseEntity<MarketSkillDetailResponse> fetchDetail(String marketId, String skillId) {
        try {
            return doFetchDetail(marketId, skillId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 执行详情查询。 */
    private ResponseEntity<MarketSkillDetailResponse> doFetchDetail(String marketId, String skillId) {
        Map<String, Object> raw = skillMarketUseCase.getDetail(marketId, skillId);
        if (raw == null || raw.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDetailResponse(raw));
    }

    /**
     * 将 Map 转换为详情响应。
     */
    private MarketSkillDetailResponse toDetailResponse(Map<String, Object> m) {
        MarketSkillDetailResponse response = new MarketSkillDetailResponse();
        BeanUtil.copyProperties(m, response);
        return response;
    }

    /**
     * 从市场安装技能。
     */
    @PostMapping("/install")
    public ResponseEntity<Map<String, String>> installFromMarket(
            @PathVariable String workspaceId,
            @RequestBody InstallMarketSkillRequest request) {
        try {
            SkillOutput result = skillMarketUseCase.installFromMarket(workspaceId, request.getMarketId(), request.getSkillId());
            return ResponseEntity.ok(Map.of("message", "安装成功", "skillId", result.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

