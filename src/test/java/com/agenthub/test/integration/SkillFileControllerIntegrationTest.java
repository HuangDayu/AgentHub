package com.agenthub.test.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
class SkillFileControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String createdSkillId;
    private final String workspaceId = "100000002";
    private final String tenantId = "100000002";
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
    void shouldCreateSkillWithUpload() throws Exception {
        byte[] zipContent = createTestZipContent();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-skill.zip",
                "application/zip",
                zipContent
        );

        String responseBody = mockMvc.perform(multipart("/api/v1/workspaces/{workspaceId}/skills/from-upload", workspaceId)
                        .file(file)
                        .param("tenantId", tenantId)
                        .param("skillCode", "test-upload-skill")
                        .param("name", "Test Upload Skill")
                        .param("description", "An uploaded skill for testing"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.skillCode").value("test-upload-skill"))
                .andExpect(jsonPath("$.skillType").value("UPLOADED"))
                .andExpect(jsonPath("$.source").value("UPLOAD"))
                .andReturn().getResponse().getContentAsString();

        createdSkillId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    void shouldListSkillFiles() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}/files", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void shouldGetSkillFileStats() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}/files/stats", workspaceId, createdSkillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileCount").exists())
                .andExpect(jsonPath("$.totalSize").exists());
    }

    @Test
    @Order(4)
    void shouldReturnEmptyFilesForNonExistentSkill() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}/files", workspaceId, "non-existent-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Order(5)
    void shouldReturnNotFoundForNonExistentFile() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(get("/api/v1/workspaces/{workspaceId}/skills/{skillId}/files/{filePath}", workspaceId, createdSkillId, "non-existent.md"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldDeleteSkill() throws Exception {
        Assertions.assertNotNull(createdSkillId, "Skill should be created first");
        mockMvc.perform(delete("/api/v1/workspaces/{workspaceId}/skills/{skillId}", workspaceId, createdSkillId))
                .andExpect(status().isNoContent());
    }

    private byte[] createTestZipContent() {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

            java.util.zip.ZipEntry entry1 = new java.util.zip.ZipEntry("SKILL.md");
            zos.putNextEntry(entry1);
            zos.write("""
                    ---
                    name: 1password-test
                    description: Set up and use 1Password CLI (op). Use when installing the CLI, enabling desktop app integration, signing in (single or multi-account), or reading/injecting/running secrets via op.
                    homepage: https://developer.1password.com/docs/cli/get-started/
                    metadata: {"clawdbot":{"emoji":"🔐","requires":{"bins":["op"]},"install":[{"id":"brew","kind":"brew","formula":"1password-cli","bins":["op"],"label":"Install 1Password CLI (brew)"}]}}
                    ---
                    
                    # 1Password CLI
                    """.getBytes());

            zos.closeEntry();

            java.util.zip.ZipEntry entry2 = new java.util.zip.ZipEntry("config.json");
            zos.putNextEntry(entry2);
            zos.write("{\"name\": \"test-skill\"}".getBytes());
            zos.closeEntry();

            zos.finish();
            zos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test ZIP", e);
        }
    }
}
