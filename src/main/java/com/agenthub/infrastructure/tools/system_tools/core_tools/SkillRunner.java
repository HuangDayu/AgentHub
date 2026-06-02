package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.port.out.tools.ToolCallbackResolverPort;
import com.agenthub.domain.model.skill.Skill;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.SkillExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 技能执行器，解析 SKILL.md 中的执行步骤并自动调用工具。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillRunner {

    private final ToolCallbackResolverPort toolCallbackResolver;
    private static final Pattern STEP_PATTERN = Pattern.compile(
            "\\d+\\.\\s*\\**([^:*]+)\\**\\s*[:：]\\s*(.+)"
    );
    private static final Pattern TOOL_REF_PATTERN = Pattern.compile("`?([a-zA-Z_][a-zA-Z0-9_]*)`?");

    public SkillExecutionResult run(Skill skill, String parameters) {
        String content = readSkillMd(skill);
        if (content == null) return fail("无法读取SKILL.md");
        List<String> steps = parseSteps(content);
        if (steps.isEmpty()) return fail("技能无执行步骤");
        List<String> outputs = new ArrayList<>();
        for (String step : steps) outputs.add(executeStep(step, parameters));
        return new SkillExecutionResult(true, "技能执行完成", outputs);
    }

    private String readSkillMd(Skill skill) {
        if (skill.getSkillPath() == null) return null;
        Path path = Paths.get(skill.getSkillPath(), "SKILL.md");
        if (!Files.exists(path)) return null;
        try { return Files.readString(path); }
        catch (IOException e) { log.error("读取SKILL.md失败", e); return null; }
    }

    private List<String> parseSteps(String content) {
        List<String> steps = new ArrayList<>();
        for (String line : content.split("\n")) {
            Matcher m = STEP_PATTERN.matcher(line.trim());
            if (m.matches()) steps.add(m.group(2).trim());
        }
        return steps;
    }

    private String executeStep(String step, String parameters) {
        Optional<ToolCallback> callback = findToolCallback(step);
        if (callback.isEmpty()) return "无法解析工具: " + step;
        return callTool(callback.get(), parameters);
    }

    private Optional<ToolCallback> findToolCallback(String step) {
        Matcher m = TOOL_REF_PATTERN.matcher(step);
        while (m.find()) {
            Optional<Object> resolved = toolCallbackResolver.resolveByName(m.group(1));
            if (resolved.isPresent() && resolved.get() instanceof ToolCallback tc) {
                return Optional.of(tc);
            }
        }
        return Optional.empty();
    }

    private String callTool(ToolCallback cb, String input) {
        try { return cb.call(input != null ? input : "{}"); }
        catch (Exception e) { return "工具调用失败: " + e.getMessage(); }
    }

    private SkillExecutionResult fail(String msg) {
        return new SkillExecutionResult(false, msg, List.of());
    }
}
