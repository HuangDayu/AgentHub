package com.agenthub.infrastructure.skills.market;

import com.agenthub.application.port.out.skills.SkillMarketPort;
import com.agenthub.application.dto.MarketSearchQuery;
import com.agenthub.domain.model.skill.MarketSkillDetail;
import com.agenthub.domain.model.skill.MarketSkillSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClawHub 技能市场适配器，解析 clawhub.ai 公开 API 响应。
 */
@Slf4j
@Component
public class ClawHubAdapter implements SkillMarketPort {

    private static final String BASE_URL = "https://clawhub.ai";
    public static final String DOWNLOAD_URL = "https://wry-manatee-359.convex.site/api/v1/download?slug=";
    private final RestTemplate restTemplate;

    /**
     * 构造 ClawHub 适配器。
     */
    public ClawHubAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getMarketId() {
        return "clawhub";
    }

    @Override
    public String getMarketName() {
        return "ClawHub";
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForEntity(BASE_URL + "/api/v1/skills?limit=1", String.class);
            return true;
        } catch (Exception e) {
            log.warn("ClawHub unavailable: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<MarketSkillSummary> search(MarketSearchQuery query) {
        try {
            String url = BASE_URL + "/api/v1/search?q=" + encode(query.getKeyword());
            Map resp = restTemplate.getForObject(url, Map.class);
            return extractResults(resp);
        } catch (Exception e) {
            log.warn("ClawHub search failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 提取搜索结果。
     * ClawHub 格式: { "results": [ { "slug", "displayName", "summary", "ownerHandle", ... } ] }
     */
    @SuppressWarnings("unchecked")
    private List<MarketSkillSummary> extractResults(Map body) {
        if (body == null) return List.of();
        List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
        if (results == null) return List.of();
        List<MarketSkillSummary> list = new ArrayList<>();
        for (Map<String, Object> item : results) {
            list.add(toSummary(item));
        }
        return list;
    }

    /**
     * 转换为摘要。
     */
    private MarketSkillSummary toSummary(Map<String, Object> item) {
        MarketSkillSummary s = new MarketSkillSummary();
        s.setMarketId("clawhub");
        s.setSkillId(str(item, "slug"));
        s.setSkillCode(str(item, "slug"));
        s.setName(str(item, "displayName"));
        s.setDescription(str(item, "summary"));
        s.setAuthor(str(item, "ownerHandle"));
        s.setVersion(str(item, "version"));
        s.setUpdatedAt(parseInstant(item.get("updatedAt")));
        return s;
    }

    @Override
    public MarketSkillDetail getDetail(String skillId) {
        try {
            String url = BASE_URL + "/api/v1/skills/" + skillId;
            Map resp = restTemplate.getForObject(url, Map.class);
            return toDetail(resp, skillId);
        } catch (Exception e) {
            log.warn("ClawHub detail failed for {}: {}", skillId, e.getMessage());
            return null;
        }
    }

    /**
     * 转换详情。
     * ClawHub detail 格式: { "skill": {...}, "owner": {...}, "latestVersion": {...} }
     */
    @SuppressWarnings("unchecked")
    private MarketSkillDetail toDetail(Object obj, String skillId) {
        if (!(obj instanceof Map root)) return null;
        Map<String, Object> skill = (Map<String, Object>) root.get("skill");
        Map<String, Object> owner = (Map<String, Object>) root.get("owner");
        Map<String, Object> stats = skill != null ? (Map<String, Object>) skill.get("stats") : null;
        if (skill == null) return null;

        MarketSkillDetail d = new MarketSkillDetail();
        d.setMarketId("clawhub");
        d.setSkillId(skillId);
        d.setName(str(skill, "displayName"));
        d.setDescription(str(skill, "summary"));
        d.setAuthor(owner != null ? str(owner, "handle") : "");
        d.setHomepage(BASE_URL + "/" + skillId);
        d.setDownloadCount(stats != null ? intVal(stats, "downloads") : 0);
        d.setStarCount(stats != null ? intVal(stats, "stars") : 0);
        d.setDownloadUrl(DOWNLOAD_URL + skillId);
        return d;
    }

    /**
     * 解析时间戳（毫秒 epoch）。
     */
    private Instant parseInstant(Object v) {
        if (v instanceof Number n) {
            return Instant.ofEpochMilli(n.longValue());
        }
        return null;
    }

    /**
     * URL 编码简易实现。
     */
    private String encode(String s) {
        return s != null ? s.replace(" ", "+") : "";
    }

    /**
     * 安全获取字符串值。
     */
    private String str(Map m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    /**
     * 安全获取整数值。
     */
    private int intVal(Map m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.intValue();
        return 0;
    }
}

