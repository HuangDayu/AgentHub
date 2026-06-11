package com.agenthub.infrastructure.skills.market;

import com.agenthub.application.port.out.skills.SkillMarketPort;
import com.agenthub.application.dto.MarketSearchQuery;
import com.agenthub.domain.model.skill.MarketSkillDetail;
import com.agenthub.domain.model.skill.MarketSkillSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GitHub 技能市场适配器，搜索 agent-skill/claude-skill 主题的仓库。
 */
@Slf4j
@Component
public class GitHubAdapter implements SkillMarketPort {

    private static final String SEARCH_URL = "https://api.github.com/search/repositories";
    private final RestTemplate restTemplate;
    private final String token;

    /**
     * 构造 GitHub 适配器。
     */
    public GitHubAdapter(
            @Value("${agenthub.skill-market.github.token:}") String token) {
        this.restTemplate = new RestTemplate();
        this.token = token;
    }

    @Override
    public String getMarketId() {
        return "github";
    }

    @Override
    public String getMarketName() {
        return "GitHub";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<MarketSkillSummary> search(MarketSearchQuery query) {
        try {
            String q = query.getKeyword() + " topic:agent-skill OR topic:claude-skill";
            String url = SEARCH_URL + "?q=" + encode(q) + "&sort=stars&per_page=" + query.getPageSize();
            ResponseEntity<Map> resp = restTemplate.exchange(
                    url, HttpMethod.GET, buildHeaders(), Map.class);
            if (resp.getBody() == null) return List.of();
            return extractItems(resp.getBody());
        } catch (Exception e) {
            log.warn("GitHub search failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 提取搜索结果条目。
     * GitHub 格式: { "items": [ { "full_name", "name", "description", "owner": {...}, ... } ] }
     */
    @SuppressWarnings("unchecked")
    private List<MarketSkillSummary> extractItems(Map body) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null) return List.of();
        List<MarketSkillSummary> result = new ArrayList<>();
        for (Map<String, Object> item : items) {
            result.add(toSummary(item));
        }
        return result;
    }

    private void setBaseFields(MarketSkillSummary s, Map<String, Object> item) {
        s.setMarketId("github");
        s.setSkillId(str(item, "full_name"));
        s.setSkillCode(str(item, "name"));
        s.setName(str(item, "name"));
        s.setDescription(str(item, "description"));
        s.setAuthor(extractAuthor(item));
        s.setStarCount(intVal(item, "stargazers_count"));
        s.setDownloadCount(intVal(item, "forks_count"));
    }

    /**
     * 转换为摘要。
     */
    @SuppressWarnings("unchecked")
    private MarketSkillSummary toSummary(Map<String, Object> item) {
        MarketSkillSummary s = new MarketSkillSummary();
        setBaseFields(s, item);
        return s;
    }

    @Override
    public MarketSkillDetail getDetail(String skillId) {
        try {
            String url = "https://api.github.com/repos/" + skillId;
            ResponseEntity<Map> resp = restTemplate.exchange(
                    url, HttpMethod.GET, buildHeaders(), Map.class);
            return toDetail(resp.getBody(), skillId);
        } catch (Exception e) {
            log.warn("GitHub detail failed: {}", e.getMessage());
            return null;
        }
    }

    private void setDetailBaseFields(MarketSkillDetail d, Map<String, Object> item, String skillId) {
        d.setMarketId("github"); d.setSkillId(skillId);
        d.setName(str(item, "name")); d.setDescription(str(item, "description"));
        d.setHomepage(str(item, "html_url")); d.setDownloadUrl(str(item, "html_url"));
        d.setStarCount(intVal(item, "stargazers_count")); d.setDownloadCount(intVal(item, "forks_count"));
        d.setAuthor(extractAuthor(item));
    }

    /**
     * 转换为详情。
     */
    @SuppressWarnings("unchecked")
    private MarketSkillDetail toDetail(Object obj, String skillId) {
        if (!(obj instanceof Map item)) return null;
        MarketSkillDetail d = new MarketSkillDetail();
        setDetailBaseFields(d, item, skillId);
        return d;
    }

    /**
     * 构建带 Accept 头的请求。
     */
    private HttpEntity<Void> buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(headers);
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

    /**
     * 提取作者。
     */
    private String extractAuthor(Map<String, Object> item) {
        Object owner = item.get("owner");
        return owner instanceof Map ownerMap ? str(ownerMap, "login") : null;
    }
}

