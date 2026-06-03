package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SkillMarketController 集成测试。
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SkillMarketControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final String workspaceId = "100000002";
    private static final String BASE = "/api/v1/workspaces/100000002/skills/market";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    /**
     * 测试获取市场列表。
     */
    @Test
    @Order(1)
    void shouldListMarkets() throws Exception {
        mockMvc.perform(get(BASE + "/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * 测试搜索市场（无结果）。
     */
    @Test
    @Order(2)
    void shouldSearchMarketsReturnsEmpty() throws Exception {
        mockMvc.perform(post(BASE + "/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "keyword": "nonexistent-skill-xyz-12345"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    /**
     * 测试搜索市场（带分页参数）。
     */
    @Test
    @Order(3)
    void shouldSearchMarketsWithPagination() throws Exception {
        mockMvc.perform(post(BASE + "/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "keyword": "test",
                                    "page": 1,
                                    "pageSize": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    /**
     * 测试获取技能详情（不存在的市场）。
     */
    @Test
    @Order(4)
    void shouldReturnNotFoundForNonExistentMarket() throws Exception {
        mockMvc.perform(get(BASE + "/detail")
                        .param("marketId", "nonexistent-market")
                        .param("skillId", "nonexistent-skill"))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试安装技能（无效市场ID）。
     */
    @Test
    @Order(5)
    void shouldFailInstallWithInvalidMarket() throws Exception {
        mockMvc.perform(post(BASE + "/install")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "marketId": "nonexistent-market",
                                    "skillId": "nonexistent-skill"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
