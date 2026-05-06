package com.agenthub.infrastructure.tools.skills_tools;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 技能文件管理器，提供技能文件系统的统一管理接口。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillFileManager {

    private final SkillValidator skillValidator;
    private final SkillInstaller skillInstaller;
    private final Path skillsRootPath;

    @Value("${agenthub.skills.auto-register:true}")
    private boolean autoRegisterSkills;

    private final Map<String, SkillPackage> skillCache = new HashMap<>();
    private volatile boolean initialized = false;

    @PostConstruct
    public void init() {
        if (autoRegisterSkills) {
            this.initialize();
        }
    }

    /**
     * 初始化技能管理器。
     */
    public synchronized void initialize() {
        if (initialized) {
            log.info("SkillFileManager already initialized");
            return;
        }

        log.info("Initializing SkillFileManager with root path: {}", skillsRootPath);

        if (!ensureRootDirectory()) return;

        scanSkills();
        initialized = true;
        log.info("SkillFileManager initialized with {} skills", skillCache.size());
    }

    /**
     * 确保根目录存在。
     */
    private boolean ensureRootDirectory() {
        if (Files.exists(skillsRootPath)) return true;

        try {
            Files.createDirectories(skillsRootPath);
            log.info("Created skills root directory: {}", skillsRootPath);
            return true;
        } catch (IOException e) {
            log.error("Failed to create skills root directory", e);
            return false;
        }
    }

    /**
     * 扫描技能目录。
     */
    public void scanSkills() {
        log.info("Scanning skills in: {}", skillsRootPath);

        try (Stream<Path> stream = Files.list(skillsRootPath)) {
            List<Path> skillPaths = stream.filter(Files::isDirectory).collect(Collectors.toList());
            Map<String, SkillPackage> newCache = loadSkills(skillPaths);
            updateCache(newCache);
            log.info("Scanned {} skills", skillCache.size());
        } catch (IOException e) {
            log.error("Failed to scan skills", e);
        }
    }

    /**
     * 加载所有技能。
     */
    private Map<String, SkillPackage> loadSkills(List<Path> skillPaths) {
        Map<String, SkillPackage> newCache = new HashMap<>();

        for (Path skillPath : skillPaths) {
            loadSkillSafely(skillPath, newCache);
        }

        return newCache;
    }

    /**
     * 安全加载技能。
     */
    private void loadSkillSafely(Path skillPath, Map<String, SkillPackage> newCache) {
        try {
            SkillPackage skillPackage = loadSkillPackage(skillPath);
            if (skillPackage != null) {
                newCache.put(skillPackage.getManifest().getCode(), skillPackage);
            }
        } catch (Exception e) {
            log.warn("Failed to load skill from: {}", skillPath, e);
        }
    }

    /**
     * 更新缓存。
     */
    private synchronized void updateCache(Map<String, SkillPackage> newCache) {
        skillCache.clear();
        skillCache.putAll(newCache);
    }

    /**
     * 加载技能包。
     */
    private SkillPackage loadSkillPackage(Path skillPath) {
        SkillValidator.ValidationResult validation = skillValidator.validate(skillPath);
        SkillPackage skillPackage = SkillPackage.of(skillPath, validation.getManifest());
        skillPackage.setInstalledAt(Instant.now());

        if (validation.isValid()) {
            skillPackage.setStatus(SkillPackage.InstallStatus.INSTALLED);
            skillPackage.setValidationStatus(SkillPackage.ValidationStatus.VALID);
        } else {
            skillPackage.setStatus(SkillPackage.InstallStatus.INSTALL_FAILED);
            skillPackage.setValidationStatus(SkillPackage.ValidationStatus.INVALID);
            skillPackage.setValidationMessage(validation.getErrorMessage());
        }

        return skillPackage;
    }

    /**
     * 获取所有已安装的技能。
     */
    public List<SkillPackage> getAllSkills() {
        ensureInitialized();
        return new ArrayList<>(skillCache.values());
    }

    /**
     * 获取所有有效的技能。
     */
    public List<SkillPackage> getValidSkills() {
        ensureInitialized();
        return skillCache.values().stream()
                .filter(SkillPackage::isValid)
                .collect(Collectors.toList());
    }

    /**
     * 根据技能代码获取技能。
     */
    public Optional<SkillPackage> getSkill(String skillCode) {
        ensureInitialized();
        return Optional.ofNullable(skillCache.get(skillCode));
    }

    /**
     * 检查技能是否已安装。
     */
    public boolean isInstalled(String skillCode) {
        ensureInitialized();
        return skillCache.containsKey(skillCode);
    }

    /**
     * 安装技能。
     */
    public SkillPackage install(Path sourcePath) {
        log.info("Installing skill from: {}", sourcePath);

        SkillInstaller.InstallResult result = skillInstaller.install(sourcePath);

        if (result.isSuccess()) {
            return handleInstallSuccess(result);
        }
        throw new SkillManagementException("Installation failed: " + result.getErrorMessage());
    }

    /**
     * 处理安装成功。
     */
    private SkillPackage handleInstallSuccess(SkillInstaller.InstallResult result) {
        SkillPackage skillPackage = loadSkillPackage(result.getInstalledPath());
        skillCache.put(skillPackage.getManifest().getCode(), skillPackage);
        return skillPackage;
    }

    /**
     * 卸载技能。
     */
    public void uninstall(String skillCode) {
        log.info("Uninstalling skill: {}", skillCode);

        if (!skillCache.containsKey(skillCode)) {
            throw new SkillManagementException("Skill not found: " + skillCode);
        }

        SkillInstaller.UninstallResult result = skillInstaller.uninstall(skillCode);

        if (result.isSuccess()) {
            skillCache.remove(skillCode);
        } else {
            throw new SkillManagementException("Uninstallation failed: " + result.getErrorMessage());
        }
    }

    /**
     * 更新技能。
     */
    public SkillPackage update(String skillCode, Path sourcePath) {
        log.info("Updating skill: {}", skillCode);

        SkillInstaller.UpdateResult result = skillInstaller.update(skillCode, sourcePath);

        if (result.isSuccess()) {
            return handleUpdateSuccess(skillCode, result);
        }
        throw new SkillManagementException("Update failed: " + result.getErrorMessage());
    }

    /**
     * 处理更新成功。
     */
    private SkillPackage handleUpdateSuccess(String skillCode, SkillInstaller.UpdateResult result) {
        SkillPackage skillPackage = loadSkillPackage(result.getUpdatedPath());
        skillCache.put(skillCode, skillPackage);
        return skillPackage;
    }

    /**
     * 校验技能。
     */
    public SkillPackage validate(String skillCode) {
        Optional<SkillPackage> skillOpt = getSkill(skillCode);

        if (skillOpt.isEmpty()) {
            throw new SkillManagementException("Skill not found: " + skillCode);
        }

        return performValidation(skillOpt.get());
    }

    /**
     * 执行校验。
     */
    private SkillPackage performValidation(SkillPackage skillPackage) {
        SkillValidator.ValidationResult validation = skillValidator.validate(skillPackage.getPath());

        if (validation.isValid()) {
            skillPackage.setValidationStatus(SkillPackage.ValidationStatus.VALID);
            skillPackage.setValidationMessage(null);
        } else {
            skillPackage.setValidationStatus(SkillPackage.ValidationStatus.INVALID);
            skillPackage.setValidationMessage(validation.getErrorMessage());
        }

        return skillPackage;
    }

    /**
     * 校验所有技能。
     */
    public List<SkillPackage> validateAll() {
        ensureInitialized();

        List<SkillPackage> results = new ArrayList<>();
        for (SkillPackage skillPackage : skillCache.values()) {
            validateSkillSafely(skillPackage, results);
        }

        return results;
    }

    /**
     * 安全校验技能。
     */
    private void validateSkillSafely(SkillPackage skillPackage, List<SkillPackage> results) {
        try {
            SkillPackage validated = validate(skillPackage.getManifest().getCode());
            results.add(validated);
        } catch (Exception e) {
            log.warn("Failed to validate skill: {}", skillPackage.getName(), e);
        }
    }

    /**
     * 获取技能数量。
     */
    public int getSkillCount() {
        return skillCache.size();
    }

    /**
     * 刷新技能缓存。
     */
    public void refresh() {
        scanSkills();
        log.info("Skill cache refreshed");
    }

    /**
     * 确保已初始化。
     */
    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
