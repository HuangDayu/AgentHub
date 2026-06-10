package com.agenthub.test.integration;

import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.agenthub.test.common.TestCommonTools.*;
import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpToolsControllerIntegrationTest {

    

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder())
                .build();
    }

    @Test
    void postAndGetTools() throws Exception {
        mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/http-tools", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"weather","description":"Weather API","enabled":true}
                                """))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name", is("weather")));

        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/http-tools", WORKSPACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("weather")))
                .andExpect(jsonPath("$[0].description", is("Weather API")));
    }

    @Test
    void getToolById() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/http-tools", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"search","description":"Search API","enabled":true}
                                """))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String toolId = extractJsonStringValue(result.getResponse().getContentAsString(), "id");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/http-tools/{toolId}", WORKSPACE_ID, toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(toolId)))
                .andExpect(jsonPath("$.name", is("search")));
    }

    @Test
    void patchToolById() throws Exception {
        // First create a tool to patch
        MvcResult result = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/http-tools", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"translate","description":"Translation API","enabled":true}
                                """))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String toolId = extractJsonStringValue(result.getResponse().getContentAsString(), "id");
        mockMvc.perform(patch("/api/v1/workspaces/{workspaceId}/http-tools/{toolId}", WORKSPACE_ID, toolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Updated","enabled":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(toolId)))
                .andExpect(jsonPath("$.name", is("translate")))
                .andExpect(jsonPath("$.description", is("Updated")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    void getToolByIdReturns404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/http-tools/{toolId}", WORKSPACE_ID, "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("tool not found: missing"));
    }

    private static String extractJsonStringValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing key in json: " + key);
        }
        return matcher.group(1);
    }
}
