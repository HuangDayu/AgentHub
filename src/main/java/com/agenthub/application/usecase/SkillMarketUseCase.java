package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateSkillCommand;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.port.out.skills.SkillMarketPort;
import com.agenthub.application.dto.MarketSearchQuery;
import com.agenthub.domain.model.skill.MarketSkillDetail;
import com.agenthub.domain.model.skill.MarketSkillSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 技能市场应用服务，并行搜索多市场。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillMarketUseCase {

    private final List<SkillMarketPort> marketPorts;
    private final SkillUseCase skillUseCase;

    /**
     * 并行搜索所有可用市场，返回 Map 嵌套结构。
     * Key: 市场ID, Value: 技能摘要 Map 列表。
     */
    public Map<String, List<Map<String, Object>>> searchAll(MarketSearchQuery query) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            return doSearchAll(query, executor);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 执行并行搜索并转换为 Map。
     */
    private Map<String, List<Map<String, Object>>> doSearchAll(
            MarketSearchQuery query, ExecutorService executor) {
        List<CompletableFuture<Map.Entry<String, List<Map<String, Object>>>>> futures =
                marketPorts.stream()
                        .filter(SkillMarketPort::isAvailable)
                        .map(port -> searchMarket(port, query, executor))
                        .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 并行搜索单个市场。
     */
    private CompletableFuture<Map.Entry<String, List<Map<String, Object>>>> searchMarket(
            SkillMarketPort port, MarketSearchQuery query, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> doSearchMarket(port, query), executor);
    }

    /**
     * 执行市场搜索并转换结果为 Map。
     */
    private Map.Entry<String, List<Map<String, Object>>> doSearchMarket(
            SkillMarketPort port, MarketSearchQuery query) {
        try {
            List<MarketSkillSummary> results = port.search(query);
            List<Map<String, Object>> mapped = results.stream()
                    .map(this::summaryToMap)
                    .collect(Collectors.toList());
            return Map.entry(port.getMarketId(), mapped);
        } catch (Exception e) {
            log.warn("Market search failed: {}", port.getMarketId(), e);
            return Map.entry(port.getMarketId(), List.of());
        }
    }

    /**
     * 将搜索摘要转换为 Map。
     */
    private Map<String, Object> summaryToMap(MarketSkillSummary s) {
        Map<String, Object> m = new HashMap<>();
        m.put("marketId", s.getMarketId());
        m.put("skillId", s.getSkillId());
        m.put("downloadCount", s.getDownloadCount());
        m.put("starCount", s.getStarCount());
        m.put("updatedAt", s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : "");
        m.putAll(stringSummaryFields(s));
        return m;
    }

    private Map<String, String> stringSummaryFields(MarketSkillSummary s) {
        return Map.ofEntries(
                Map.entry("skillCode", nullToEmpty(s.getSkillCode())),
                Map.entry("name", nullToEmpty(s.getName())),
                Map.entry("description", nullToEmpty(s.getDescription())),
                Map.entry("author", nullToEmpty(s.getAuthor())),
                Map.entry("version", nullToEmpty(s.getVersion())),
                Map.entry("thumbnailUrl", nullToEmpty(s.getThumbnailUrl())));
    }

    /**
     * 获取市场信息列表。
     */
    public List<Map<String, String>> listMarkets() {
        return marketPorts.stream()
                .filter(SkillMarketPort::isAvailable)
                .map(this::toMarketInfo)
                .collect(Collectors.toList());
    }

    /**
     * 转换市场信息。
     */
    private Map<String, String> toMarketInfo(SkillMarketPort port) {
        return Map.of("marketId", port.getMarketId(), "marketName", port.getMarketName());
    }

    /**
     * 获取技能详情，返回 Map。
     */
    public Map<String, Object> getDetail(String marketId, String skillId) {
        SkillMarketPort port = findPort(marketId);
        MarketSkillDetail detail = port.getDetail(skillId);
        if (detail == null) {
            return Map.of();
        }
        return detailToMap(detail, marketId);
    }

    /**
     * 将详情转换为 Map。
     */
    private Map<String, Object> detailToMap(MarketSkillDetail d, String marketId) {
        Map<String, Object> m = new HashMap<>();
        m.put("marketId", marketId);
        m.put("tags", d.getTags() != null ? d.getTags() : List.of());
        putCountAndDate(m, d);
        m.putAll(identityFields(d));
        m.putAll(extraFields(d));
        return m;
    }

    private void putCountAndDate(Map<String, Object> m, MarketSkillDetail d) {
        m.put("downloadCount", d.getDownloadCount());
        m.put("starCount", d.getStarCount());
        m.put("updatedAt", d.getUpdatedAt() != null ? d.getUpdatedAt().toString() : "");
    }

    private Map<String, String> identityFields(MarketSkillDetail d) {
        return Map.ofEntries(
                Map.entry("skillId", nullToEmpty(d.getSkillId())),
                Map.entry("skillCode", nullToEmpty(d.getSkillCode())),
                Map.entry("name", nullToEmpty(d.getName())),
                Map.entry("author", nullToEmpty(d.getAuthor())),
                Map.entry("version", nullToEmpty(d.getVersion())));
    }

    private Map<String, String> extraFields(MarketSkillDetail d) {
        return Map.ofEntries(
                Map.entry("description", nullToEmpty(d.getDescription())),
                Map.entry("license", nullToEmpty(d.getLicense())),
                Map.entry("homepage", nullToEmpty(d.getHomepage())),
                Map.entry("downloadUrl", nullToEmpty(d.getDownloadUrl())),
                Map.entry("readmeContent", nullToEmpty(d.getReadmeContent())));
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /**
     * 查找指定市场的端口。
     */
    private SkillMarketPort findPort(String marketId) {
        return marketPorts.stream()
                .filter(p -> p.getMarketId().equals(marketId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知市场: " + marketId));
    }

    /**
     * 从市场安装技能。
     */
    public SkillOutput installFromMarket(String workspaceId, String marketId, String skillId) {
        SkillMarketPort port = findPort(marketId);
        MarketSkillDetail detail = port.getDetail(skillId);
        if (detail == null || detail.getDownloadUrl() == null) {
            throw new IllegalArgumentException("无法获取技能下载地址");
        }
        CreateSkillCommand command = buildInstallCommand(workspaceId, detail);
        return skillUseCase.createFromUrl(command);
    }

    /**
     * 构建安装命令。
     */
    private CreateSkillCommand buildInstallCommand(String workspaceId, MarketSkillDetail detail) {
        CreateSkillCommand command = new CreateSkillCommand();
        command.setWorkspaceId(workspaceId);
        command.setSkillType("UPLOADED");
        command.setSource("MARKET");
        setDetailFields(command, detail);
        return command;
    }

    private void setDetailFields(CreateSkillCommand command, MarketSkillDetail detail) {
        command.setSkillCode(detail.getSkillCode());
        command.setName(detail.getName());
        command.setDescription(detail.getDescription());
        command.setSourcePath(detail.getDownloadUrl());
        command.setZipUrl(detail.getDownloadUrl());
    }
}

