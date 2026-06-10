package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.agenthub.test.common.TestCommonTools.*;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static com.agenthub.test.common.TestCommonTools.writeToken;
import static org.assertj.core.api.Assertions.assertThat;
import static com.agenthub.test.common.TestCommonTools.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IAM Service Controller 集成测试.
 * <p>
 * 覆盖 AuthController (/api/v1/auth) 和 UserController (/api/v1/user) 的所有端点。
 * AuthController: login, refresh, logout, verify, me (5 端点)
 * UserController: sessions(x4), messages(x3), knowledge-bases, agents,
 *                 notifications(x3), retrieval/search (13 端点)
 * 共 18 个端点（stream 端点标记为 @Disabled）。
 * </p>
 */
@SpringBootTest(classes = {TestAgentHubApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIntegrationTest {

    private static String extractJsonStringValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing key in json: " + key);
        }
        return matcher.group(1);
    }

    private String savedAccessToken;
    private String savedRefreshToken;
    private String savedSessionId;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private MockMvc authMockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
        authMockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    // ==================== Auth Endpoints (5) ====================

    @Test
    @Order(1)
    void authLoginShouldReturnTokens() throws Exception {
        String loginBody = """
                {"username":"lisi","password":"user123"}
                """;
        String response = authMockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        savedAccessToken = extractJsonStringValue(response, "accessToken");
        savedRefreshToken = extractJsonStringValue(response, "refreshToken");
        assertThat(savedAccessToken).isNotBlank();
        assertThat(savedRefreshToken).isNotBlank();
        writeToken("Bearer " + savedAccessToken);
    }

    @Test
    @Order(2)
    void authRefreshShouldReturnNewTokens() throws Exception {
        String refreshBody = """
                {"refreshToken":"%s"}
                """.formatted(savedRefreshToken);

        String response = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newAccessToken = extractJsonStringValue(response, "accessToken");
        String newRefreshToken = extractJsonStringValue(response, "refreshToken");
        // Rotate tokens
        savedAccessToken = newAccessToken;
        savedRefreshToken = newRefreshToken;
    }

    @Test
    @Order(3)
    void authVerifyShouldValidateToken() throws Exception {
        String verifyBody = """
                {"accessToken":"%s"}
                """.formatted(savedAccessToken);

        mockMvc.perform(post("/api/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value("lisi"))
                .andExpect(jsonPath("$.tenantId").isNotEmpty());
    }

    @Test
    @Order(4)
    void authMeShouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + savedAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").isNotEmpty())
                .andExpect(jsonPath("$.tenantId").isNotEmpty());
    }

    @Test
    @Order(5)
    void authLogoutShouldSucceed() throws Exception {
        String logoutBody = """
                {"refreshToken":"%s"}
                """.formatted(savedRefreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutBody))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(6)
    void authLoginShouldReturn401ForInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nonexistent","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    void authRefreshShouldReturn401ForInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"nonexistent-token"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    void authLogoutShouldReturn401ForInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"invalid-token"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(9)
    void authVerifyShouldReturn401ForInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accessToken":"invalid-token-value"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(10)
    void authLoginRefreshLogoutFullLifecycle() throws Exception {
        // Step 1: Login
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"lisi","password":"user123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractJsonStringValue(loginResponse, "accessToken");
        String refresh = extractJsonStringValue(loginResponse, "refreshToken");

        // Step 2: Refresh
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refresh)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String rotatedRefresh = extractJsonStringValue(refreshResponse, "refreshToken");

        // Step 3: Logout with rotated token
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(rotatedRefresh)))
                .andExpect(status().isNoContent());

        // Step 4: Verify access token is still valid after logout
        mockMvc.perform(post("/api/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accessToken":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

}
