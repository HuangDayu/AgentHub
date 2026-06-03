package com.agenthub.infrastructure.skills.market;

import com.agenthub.application.port.out.skills.SkillMarketPort;
import com.agenthub.domain.model.skill.MarketSearchQuery;
import com.agenthub.domain.model.skill.MarketSkillDetail;
import com.agenthub.domain.model.skill.MarketSkillSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SkillsMP 技能市场适配器，解析 skillsmp.com 公开 API 响应。
 * SkillsMP 仅提供搜索接口，无详情接口，通过缓存搜索结果实现详情查询。
 */
@Slf4j
@Component
public class SkillsMPAdapter implements SkillMarketPort {

    private static final String BASE_URL = "https://skillsmp.com";
    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<String, Map<String, Object>> searchCache = new ConcurrentHashMap<>();

    /**
     * 构造 SkillsMP 适配器。
     */
    public SkillsMPAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getMarketId() {
        return "skillsmp";
    }

    @Override
    public String getMarketName() {
        return "SkillsMP";
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForEntity(
                    BASE_URL + "/api/v1/skills/search?q=test&limit=1", String.class);
            return true;
        } catch (Exception e) {
            log.warn("SkillsMP unavailable: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<MarketSkillSummary> search(MarketSearchQuery query) {
        try {
            String url = BASE_URL + "/api/v1/skills/search?q=" + encode(query.getKeyword())
                    + "&page=" + query.getPage() + "&limit=" + query.getPageSize();
            Map resp = restTemplate.getForObject(url, Map.class);
            List<MarketSkillSummary> results = extractSkills(resp);
            cacheSearchResults(results, resp);
            return results;
        } catch (Exception e) {
            log.warn("SkillsMP search failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 缓存搜索结果原始数据，供 getDetail 使用。
     */
    @SuppressWarnings("unchecked")
    private void cacheSearchResults(List<MarketSkillSummary> results, Map resp) {
        if (resp == null) return;
        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        if (data == null) return;
        List<Map<String, Object>> skills = (List<Map<String, Object>>) data.get("skills");
        if (skills == null) return;
        for (Map<String, Object> item : skills) {
            Object id = item.get("id");
            if (id != null) {
                searchCache.put(id.toString(), item);
            }
        }
    }

    /**
     * 提取搜索结果。
     * SkillsMP 格式: { "data": { "skills": [ { "id", "name", "author", "description", ... } ] } }
     */
    @SuppressWarnings("unchecked")
    private List<MarketSkillSummary> extractSkills(Map body) {
        if (body == null) return List.of();
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        if (data == null) return List.of();
        List<Map<String, Object>> skills = (List<Map<String, Object>>) data.get("skills");
        if (skills == null) return List.of();
        List<MarketSkillSummary> list = new ArrayList<>();
        for (Map<String, Object> item : skills) {
            list.add(toSummary(item));
        }
        return list;
    }

    /**
     * 转换为摘要。
     */
    private MarketSkillSummary toSummary(Map<String, Object> item) {
        MarketSkillSummary s = new MarketSkillSummary();
        s.setMarketId("skillsmp");
        s.setSkillId(str(item, "id"));
        s.setSkillCode(str(item, "name"));
        s.setName(str(item, "name"));
        s.setDescription(str(item, "description"));
        s.setAuthor(str(item, "author"));
        s.setStarCount(intVal(item, "stars"));
        s.setUpdatedAt(parseInstant(str(item, "updatedAt")));
        return s;
    }

    @Override
    public MarketSkillDetail getDetail(String skillId) {
        Map<String, Object> cached = searchCache.get(skillId);
        if (cached != null) {
            return toDetail(cached, skillId);
        }
        return searchAndCache(skillId);
    }

    /**
     * SkillsMP 无详情接口，通过搜索名称缓存并返回。
     */
    private MarketSkillDetail searchAndCache(String skillId) {
        try {
            String url = BASE_URL + "/api/v1/skills/search?q=" + encode(skillId) + "&limit=20";
            Map resp = restTemplate.getForObject(url, Map.class);
            if (resp == null) return null;
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) return null;
            List<Map<String, Object>> skills = (List<Map<String, Object>>) data.get("skills");
            if (skills == null) return null;
            for (Map<String, Object> item : skills) {
                if (skillId.equals(str(item, "id"))) {
                    searchCache.put(skillId, item);
                    return toDetail(item, skillId);
                }
            }
            if (!skills.isEmpty()) {
                Map<String, Object> first = skills.get(0);
                searchCache.put(skillId, first);
                return toDetail(first, skillId);
            }
            return null;
        } catch (Exception e) {
            log.warn("SkillsMP search for detail failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 转换详情。
     */
    private MarketSkillDetail toDetail(Object obj, String skillId) {
        if (!(obj instanceof Map m)) return null;
        MarketSkillDetail d = new MarketSkillDetail();
        d.setMarketId("skillsmp");
        d.setSkillId(skillId);
        d.setSkillCode(str(m, "name"));
        d.setName(str(m, "name"));
        d.setDescription(str(m, "description"));
        d.setAuthor(str(m, "author"));
        d.setVersion(str(m, "version"));
        d.setDownloadUrl(str(m, "skillUrl"));
        d.setStarCount(intVal(m, "stars"));
        d.setHomepage(str(m, "githubUrl"));
        d.setReadmeContent(str(m, "content"));
        return d;
    }

    /**
     * 解析时间戳。
     */
    private Instant parseInstant(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            long millis = Long.parseLong(s);
            return Instant.ofEpochMilli(millis);
        } catch (Exception e) {
            return null;
        }
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
