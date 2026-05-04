package com.agenthub.infrastructure.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 技能校验器，负责校验技能包的完整性和有效性。
 * 
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillValidator {
    
    private final ObjectMapper objectMapper;
    
    /**
     * 校验技能包。
     */
    public ValidationResult validate(Path skillPath) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (!validatePath(skillPath, errors)) {
            return ValidationResult.failure(errors, warnings);
        }
        
        Path manifestFile = findManifestFile(skillPath);
        if (manifestFile == null) {
            errors.add("Skill manifest file (skill.json or skill.yaml) not found");
            return ValidationResult.failure(errors, warnings);
        }
        
        return validateManifestFile(manifestFile, skillPath, errors, warnings);
    }
    
    /**
     * 校验路径。
     */
    private boolean validatePath(Path skillPath, List<String> errors) {
        if (!Files.exists(skillPath)) {
            errors.add("Skill path does not exist: " + skillPath);
            return false;
        }
        if (!Files.isDirectory(skillPath)) {
            errors.add("Skill path is not a directory: " + skillPath);
            return false;
        }
        return true;
    }
    
    /**
     * 校验清单文件。
     */
    private ValidationResult validateManifestFile(Path manifestFile, Path skillPath, 
                                                  List<String> errors, List<String> warnings) {
        SkillManifest manifest = parseManifestSafely(manifestFile, errors);
        if (manifest == null) {
            return ValidationResult.failure(errors, warnings);
        }
        
        validateManifestContent(manifest, errors, warnings);
        validateEntrypoint(manifest, skillPath, errors);
        validateDependencies(manifest, errors, warnings);
        
        return errors.isEmpty() 
            ? ValidationResult.success(manifest, warnings)
            : ValidationResult.failure(errors, warnings);
    }
    
    /**
     * 安全解析清单。
     */
    private SkillManifest parseManifestSafely(Path manifestFile, List<String> errors) {
        try {
            return parseManifest(manifestFile);
        } catch (Exception e) {
            errors.add("Failed to parse skill manifest: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 查找清单文件。
     */
    private Path findManifestFile(Path skillPath) {
        Path jsonFile = skillPath.resolve("skill.json");
        if (Files.exists(jsonFile)) return jsonFile;
        
        Path yamlFile = skillPath.resolve("skill.yaml");
        if (Files.exists(yamlFile)) return yamlFile;
        
        Path ymlFile = skillPath.resolve("skill.yml");
        if (Files.exists(ymlFile)) return ymlFile;
        
        return null;
    }
    
    /**
     * 解析清单文件。
     */
    private SkillManifest parseManifest(Path manifestFile) throws IOException {
        String content = Files.readString(manifestFile);
        return objectMapper.readValue(content, SkillManifest.class);
    }
    
    /**
     * 校验清单内容。
     */
    private void validateManifestContent(SkillManifest manifest, 
                                        List<String> errors, List<String> warnings) {
        validateRequiredFields(manifest, errors);
        validateOptionalFields(manifest, warnings);
        validateFieldFormats(manifest, errors, warnings);
    }
    
    /**
     * 校验必填字段。
     */
    private void validateRequiredFields(SkillManifest manifest, List<String> errors) {
        if (manifest.getId() == null || manifest.getId().isBlank()) {
            errors.add("Skill id is required");
        }
        if (manifest.getCode() == null || manifest.getCode().isBlank()) {
            errors.add("Skill code is required");
        }
        if (manifest.getName() == null || manifest.getName().isBlank()) {
            errors.add("Skill name is required");
        }
    }
    
    /**
     * 校验可选字段。
     */
    private void validateOptionalFields(SkillManifest manifest, List<String> warnings) {
        if (manifest.getVersion() == null || manifest.getVersion().isBlank()) {
            warnings.add("Skill version is not specified");
        }
        if (manifest.getType() == null || manifest.getType().isBlank()) {
            warnings.add("Skill type is not specified");
        }
    }
    
    /**
     * 校验字段格式。
     */
    private void validateFieldFormats(SkillManifest manifest, 
                                     List<String> errors, List<String> warnings) {
        if (manifest.getCode() != null && !manifest.getCode().matches("^[a-z0-9-]+$")) {
            errors.add("Skill code must only contain lowercase letters, numbers, and hyphens");
        }
        if (manifest.getVersion() != null && !manifest.getVersion().matches("^\\d+\\.\\d+\\.\\d+.*")) {
            warnings.add("Skill version should follow semantic versioning (e.g., 1.0.0)");
        }
    }
    
    /**
     * 校验入口文件。
     */
    private void validateEntrypoint(SkillManifest manifest, Path skillPath, List<String> errors) {
        if (manifest.getEntrypoint() == null) return;
        
        Path entrypointPath = skillPath.resolve(manifest.getEntrypoint());
        if (!Files.exists(entrypointPath)) {
            errors.add("Entrypoint file not found: " + manifest.getEntrypoint());
        }
    }
    
    /**
     * 校验依赖。
     */
    private void validateDependencies(SkillManifest manifest, 
                                     List<String> errors, List<String> warnings) {
        if (manifest.getDependencies() == null) return;
        
        for (SkillManifest.Dependency dependency : manifest.getDependencies()) {
            validateDependency(dependency, errors, warnings);
        }
    }
    
    /**
     * 校验单个依赖。
     */
    private void validateDependency(SkillManifest.Dependency dependency, 
                                   List<String> errors, List<String> warnings) {
        if (dependency.getName() == null || dependency.getName().isBlank()) {
            errors.add("Dependency name is required");
        }
        if (dependency.getVersion() == null || dependency.getVersion().isBlank()) {
            warnings.add("Dependency version is not specified for: " + dependency.getName());
        }
    }
    
    /**
     * 校验结果。
     */
    public static class ValidationResult {
        private final boolean valid;
        private final SkillManifest manifest;
        private final List<String> errors;
        private final List<String> warnings;
        
        private ValidationResult(boolean valid, SkillManifest manifest, 
                                List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.manifest = manifest;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public static ValidationResult success(SkillManifest manifest, List<String> warnings) {
            return new ValidationResult(true, manifest, List.of(), warnings);
        }
        
        public static ValidationResult failure(List<String> errors, List<String> warnings) {
            return new ValidationResult(false, null, errors, warnings);
        }
        
        public boolean isValid() { return valid; }
        public SkillManifest getManifest() { return manifest; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public String getErrorMessage() { return String.join("; ", errors); }
    }
}
