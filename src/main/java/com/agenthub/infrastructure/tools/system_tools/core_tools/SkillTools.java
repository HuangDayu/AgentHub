package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.skill.Skill;
import com.agenthub.domain.model.skill.SkillFile;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.AgentSkillDTO;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.SkillDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 技能数据域工具，提供技能信息查询、详情获取和文件读取功能。
 */
@RequiredArgsConstructor
@AgentTools(name = "SkillTools", description = "技能数据工具，提供技能信息查询、详情获取和文件读取功能")
public class SkillTools {

    private final SkillRepository skillRepository;
    private final DocumentFileStoragePort documentFileStoragePort;
    private final SkillFileRepository skillFileRepository;

    @Tool(description = "获取当前工作空间下Agent可用的技能列表")
    public List<AgentSkillDTO> getSkills(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return skillRepository.findByWorkspaceId(ctx.getWorkspace().getWorkspace().getId()).stream()
                .map(this::toBasicDto)
                .collect(Collectors.toList());
    }

    @Tool(description = "获取技能详情，包含文件路径和文件树结构")
    public SkillDetailDTO getSkillDetail(@ToolParam(description = "技能标识") String skillCode) {
        Skill skill = findSkill(skillCode);
        return toDetailDto(skill);
    }

    @Tool(description = "读取技能的SKILL.md文件内容，了解技能的使用说明")
    public String readSkillDocumentation(@ToolParam(description = "技能标识") String skillCode) {
        Skill skill = findSkill(skillCode);
        return readSkillFileFromMinio(skill, "SKILL.md");
    }

    @Tool(description = "读取技能中的指定文件（从MinIO存储读取）")
    public String readSkillFile(@ToolParam(description = "技能标识") String skillCode,
                                @ToolParam(description = "文件路径（相对于技能目录）") String filePath) {
        Skill skill = findSkill(skillCode);
        return readSkillFileFromMinio(skill, filePath);
    }

    @Tool(description = "列出技能下的所有文件（从数据库查询）")
    public String listSkillFiles(
            @ToolParam(description = "技能标识") String skillCode) {
        List<SkillFile> files = skillFileRepository.findBySkillId(findSkill(skillCode).getId());
        return formatFileList(files);
    }

    @Tool(description = "搜索技能名称或描述中包含关键词的技能")
    public List<AgentSkillDTO> searchSkills(
            @ToolParam(description = "搜索关键词") String keyword,
            ToolContext toolContext) {
        return skillRepository.search(keyword)
                .stream().map(this::toBasicDto).collect(Collectors.toList());
    }

    private Skill findSkill(String skillCode) {
        return skillRepository.findBySkillCode(skillCode)
                .orElseThrow(() -> new com.agenthub.domain.exception.NotFoundException("技能不存在: " + skillCode));
    }

    /**
     * 从 MinIO 读取技能文件内容。
     */
    private String readSkillFileFromMinio(Skill skill, String filePath) {
        String storagePath = buildStoragePath(skill.getSkillCode(), filePath);
        try (InputStream is = documentFileStoragePort.retrieve(storagePath)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "读取文件失败: " + e.getMessage();
        }
    }

    /**
     * 构建 MinIO 存储路径。
     */
    private String buildStoragePath(String skillCode, String filePath) {
        return String.format("skills/%s/%s", skillCode, filePath);
    }

    /**
     * 格式化文件列表为字符串。
     */
    private String formatFileList(List<SkillFile> files) {
        if (files.isEmpty()) {
            return "技能没有关联的文件";
        }
        StringBuilder sb = new StringBuilder();
        for (SkillFile f : files) {
            sb.append(f.getFilePath());
            sb.append(" (").append(f.getFileSize()).append(" bytes)\n");
        }
        return sb.toString();
    }

    private AgentSkillDTO toBasicDto(Skill skill) {
        AgentSkillDTO dto = new AgentSkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setDescription(skill.getDescription());
        dto.setSkillType(skill.getSkillType());
        dto.setEnabled(skill.isEnabled());
        dto.setSkillCode(skill.getSkillCode());
        dto.setFileCount(skill.getFileCount());
        dto.setTotalSize(skill.getTotalSize());
        return dto;
    }

    private SkillDetailDTO toDetailDto(Skill skill) {
        SkillDetailDTO dto = new SkillDetailDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setDescription(skill.getDescription());
        dto.setSkillType(skill.getSkillType());
        dto.setSkillPath(skill.getSkillPath());
        dto.setSkillFilesTree(skill.getSkillFilesTree());
        dto.setEnabled(skill.isEnabled());
        dto.setFileCount(skill.getFileCount());
        dto.setTotalSize(skill.getTotalSize());
        dto.setLastSyncAt(skill.getLastSyncAt());
        return dto;
    }
}
