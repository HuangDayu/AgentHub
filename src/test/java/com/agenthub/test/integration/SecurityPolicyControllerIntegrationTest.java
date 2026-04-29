package com.agenthub.test.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityPolicyControllerIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdPolicyId;
    private final String workspaceId = "100000002";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateSecurityPolicy() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/security-policies", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "100000001",
                                    "workspaceId": "100000002",
                                    "name": "Test Security Policy",
                                    "description": "Test security policy description"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Security Policy"))
                .andReturn().getResponse().getContentAsString();

        createdPolicyId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListSecurityPolicies() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/security-policies", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetSecurityPolicyById() throws Exception {
        Assertions.assertNotNull(createdPolicyId, "SecurityPolicy should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/security-policies/{policyId}", workspaceId, createdPolicyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdPolicyId));
    }

    @Test
    @Order(4)
    void shouldUpdateSecurityPolicy() throws Exception {
        Assertions.assertNotNull(createdPolicyId, "SecurityPolicy should be created first");
        mockMvc.perform(put("/api/v1/workspaces/{workspaceId}/security-policies/{policyId}", workspaceId, createdPolicyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Security Policy",
                                    "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Security Policy"));
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/security-policies/{policyId}", workspaceId, "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteSecurityPolicy() throws Exception {
        Assertions.assertNotNull(createdPolicyId, "SecurityPolicy should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/security-policies/{policyId}", workspaceId, createdPolicyId))
                .andExpect(status().isNoContent());
    }
}
