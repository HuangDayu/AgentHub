package com.agenthub.infrastructure.tools.system_tools.core_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.tools.Skill;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.AgentSkillDTO;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.SkillDetailDTO;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.SkillExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final SkillRunner skillRunner;

    @Tool(description = "获取当前工作空间下Agent可用的技能列表")
    public List<AgentSkillDTO> getSkills(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return skillRepository.findByWorkspaceId(ctx.getWorkspace().getWorkspace().getId()).stream()
                .map(this::toBasicDto)
                .collect(Collectors.toList());
    }

    @Tool(description = "获取技能详情，包含文件路径和文件树结构")
    public SkillDetailDTO getSkillDetail(
            @ToolParam(description = "技能ID") String skillId) {
        Skill skill = findSkill(skillId);
        return toDetailDto(skill);
    }

    @Tool(description = "读取技能的SKILL.md文件内容，了解技能的使用说明")
    public String readSkillDocumentation(
            @ToolParam(description = "技能ID") String skillId) {
        Skill skill = findSkill(skillId);
        return readSkillFile(skill, "SKILL.md");
    }

    @Tool(description = "读取技能目录下的指定文件")
    public String readSkillFile(
            @ToolParam(description = "技能ID") String skillId,
            @ToolParam(description = "文件路径（相对于技能目录）") String filePath) {
        Skill skill = findSkill(skillId);
        return readSkillFile(skill, filePath);
    }

    @Tool(description = "列出技能目录下的所有文件")
    public String listSkillFiles(
            @ToolParam(description = "技能ID") String skillId) {
        Skill skill = findSkill(skillId);
        if (skill.getSkillPath() == null || skill.getSkillPath().isBlank()) {
            return "技能没有关联的文件路径";
        }
        Path skillDir = Paths.get(skill.getSkillPath());
        if (!Files.exists(skillDir)) {
            return "技能目录不存在: " + skill.getSkillPath();
        }
        return listFilesRecursive(skillDir, skillDir);
    }

    @Tool(description = "搜索技能名称或描述中包含关键词的技能")
    public List<AgentSkillDTO> searchSkills(
            @ToolParam(description = "搜索关键词") String keyword,
            ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return skillRepository.findByWorkspaceId(ctx.getWorkspace().getWorkspace().getId())
                .stream()
                .filter(s -> matchesKeyword(s, keyword))
                .map(this::toBasicDto)
                .collect(Collectors.toList());
    }

    @Tool(description = "执行技能，解析SKILL.md中的步骤并自动调用工具")
    public SkillExecutionResult executeSkill(
            @ToolParam(description = "技能ID") String skillId,
            @ToolParam(description = "执行参数（JSON格式）") String parameters) {
        Skill skill = findSkill(skillId);
        return skillRunner.run(skill, parameters);
    }

    private Skill findSkill(String skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new com.agenthub.domain.exception.NotFoundException(
                        "技能不存在: " + skillId));
    }

    private String readSkillFile(Skill skill, String filePath) {
        if (skill.getSkillPath() == null || skill.getSkillPath().isBlank()) {
            return "技能没有关联的文件路径";
        }
        Path skillDir = Paths.get(skill.getSkillPath()).toAbsolutePath().normalize();
        Path file = skillDir.resolve(filePath).normalize();
        if (!file.startsWith(skillDir)) {
            return "不允许访问技能目录外的文件";
        }
        if (!Files.exists(file)) return "文件不存在: " + filePath;
        try { return Files.readString(file); }
        catch (IOException e) { return "读取文件失败: " + e.getMessage(); }
    }

    private String listFilesRecursive(Path current, Path root) {
        StringBuilder sb = new StringBuilder();
        try {
            Files.list(current).sorted().forEach(path -> {
                String relative = root.relativize(path).toString();
                if (Files.isDirectory(path)) {
                    sb.append(relative).append("/\n");
                    sb.append(listFilesRecursive(path, root));
                } else {
                    sb.append(relative).append("\n");
                }
            });
        } catch (IOException e) {
            sb.append("读取目录失败: ").append(e.getMessage());
        }
        return sb.toString();
    }

    private boolean matchesKeyword(Skill skill, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String lower = keyword.toLowerCase();
        return (skill.getName() != null && skill.getName().toLowerCase().contains(lower))
                || (skill.getDescription() != null && skill.getDescription().toLowerCase().contains(lower))
                || (skill.getSkillType() != null && skill.getSkillType().toLowerCase().contains(lower));
    }

    private AgentSkillDTO toBasicDto(Skill skill) {
        AgentSkillDTO dto = new AgentSkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setDescription(skill.getDescription());
        dto.setSkillType(skill.getSkillType());
        dto.setEnabled(skill.isEnabled());
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
        return dto;
    }
}
