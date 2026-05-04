package com.agenthub.infrastructure.skills;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * 技能安装器，负责技能的安装、卸载和更新。
 * 
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillInstaller {
    
    private final SkillValidator skillValidator;
    private final Path skillsRootPath;
    
    /**
     * 安装技能。
     */
    public InstallResult install(Path sourcePath) {
        log.info("Installing skill from: {}", sourcePath);
        
        SkillValidator.ValidationResult validation = skillValidator.validate(sourcePath);
        if (!validation.isValid()) {
            return InstallResult.failure("Validation failed: " + validation.getErrorMessage());
        }
        
        return performInstall(sourcePath, validation.getManifest());
    }
    
    /**
     * 执行安装。
     */
    private InstallResult performInstall(Path sourcePath, SkillManifest manifest) {
        String skillCode = manifest.getCode();
        Path targetPath = skillsRootPath.resolve(skillCode);
        
        if (Files.exists(targetPath)) {
            log.warn("Skill already installed: {}", skillCode);
            return InstallResult.failure("Skill already installed: " + skillCode);
        }
        
        try {
            return doInstall(sourcePath, targetPath, manifest);
        } catch (IOException e) {
            return handleInstallFailure(skillCode, targetPath, e);
        }
    }
    
    /**
     * 执行实际安装操作。
     */
    private InstallResult doInstall(Path sourcePath, Path targetPath, SkillManifest manifest) 
            throws IOException {
        Files.createDirectories(targetPath);
        copyDirectory(sourcePath, targetPath);
        manifest.setInstalledAt(Instant.now());
        
        log.info("Skill installed successfully: {} at {}", manifest.getCode(), targetPath);
        return InstallResult.success(manifest, targetPath);
    }
    
    /**
     * 处理安装失败。
     */
    private InstallResult handleInstallFailure(String skillCode, Path targetPath, IOException e) {
        log.error("Failed to install skill: {}", skillCode, e);
        cleanup(targetPath);
        return InstallResult.failure("Installation failed: " + e.getMessage());
    }
    
    /**
     * 卸载技能。
     */
    public UninstallResult uninstall(String skillCode) {
        log.info("Uninstalling skill: {}", skillCode);
        
        Path skillPath = skillsRootPath.resolve(skillCode);
        if (!Files.exists(skillPath)) {
            log.warn("Skill not found: {}", skillCode);
            return UninstallResult.failure("Skill not found: " + skillCode);
        }
        
        try {
            deleteDirectory(skillPath);
            log.info("Skill uninstalled successfully: {}", skillCode);
            return UninstallResult.success(skillCode);
        } catch (IOException e) {
            log.error("Failed to uninstall skill: {}", skillCode, e);
            return UninstallResult.failure("Uninstallation failed: " + e.getMessage());
        }
    }
    
    /**
     * 更新技能。
     */
    public UpdateResult update(String skillCode, Path sourcePath) {
        log.info("Updating skill: {} from {}", skillCode, sourcePath);
        
        SkillValidator.ValidationResult validation = skillValidator.validate(sourcePath);
        if (!validation.isValid()) {
            return UpdateResult.failure("Validation failed: " + validation.getErrorMessage());
        }
        
        return performUpdate(skillCode, sourcePath, validation.getManifest());
    }
    
    /**
     * 执行更新。
     */
    private UpdateResult performUpdate(String skillCode, Path sourcePath, SkillManifest manifest) {
        Path targetPath = skillsRootPath.resolve(skillCode);
        
        if (!Files.exists(targetPath)) {
            return handleInstallInstead(skillCode, sourcePath, manifest, targetPath);
        }
        
        try {
            return doUpdate(sourcePath, targetPath, manifest);
        } catch (IOException e) {
            log.error("Failed to update skill: {}", skillCode, e);
            return UpdateResult.failure("Update failed: " + e.getMessage());
        }
    }
    
    /**
     * 处理未安装时执行安装。
     */
    private UpdateResult handleInstallInstead(String skillCode, Path sourcePath, 
                                             SkillManifest manifest, Path targetPath) {
        log.warn("Skill not installed, performing install instead: {}", skillCode);
        InstallResult installResult = install(sourcePath);
        
        if (installResult.isSuccess()) {
            return UpdateResult.success(installResult.getManifest(), targetPath);
        }
        return UpdateResult.failure(installResult.getErrorMessage());
    }
    
    /**
     * 执行实际更新操作。
     */
    private UpdateResult doUpdate(Path sourcePath, Path targetPath, SkillManifest manifest) 
            throws IOException {
        Path backupPath = createBackup(targetPath);
        deleteDirectory(targetPath);
        
        Files.createDirectories(targetPath);
        copyDirectory(sourcePath, targetPath);
        deleteDirectory(backupPath);
        
        manifest.setUpdatedAt(Instant.now());
        log.info("Skill updated successfully: {}", manifest.getCode());
        return UpdateResult.success(manifest, targetPath);
    }
    
    /**
     * 复制目录。
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(sourcePath -> copyFile(source, target, sourcePath));
        }
    }
    
    /**
     * 复制单个文件。
     */
    private void copyFile(Path source, Path target, Path sourcePath) {
        try {
            Path targetPath = target.resolve(source.relativize(sourcePath));
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file: " + sourcePath, e);
        }
    }
    
    /**
     * 删除目录。
     */
    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        
        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted((a, b) -> -a.compareTo(b))
                  .forEach(this::deleteFile);
        }
    }
    
    /**
     * 删除文件。
     */
    private void deleteFile(Path p) {
        try {
            Files.delete(p);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete: " + p, e);
        }
    }
    
    /**
     * 创建备份。
     */
    private Path createBackup(Path path) throws IOException {
        Path backupPath = path.resolveSibling(path.getFileName() + ".backup");
        if (Files.exists(backupPath)) {
            deleteDirectory(backupPath);
        }
        copyDirectory(path, backupPath);
        return backupPath;
    }
    
    /**
     * 清理失败的安装。
     */
    private void cleanup(Path path) {
        try {
            deleteDirectory(path);
        } catch (IOException e) {
            log.warn("Failed to cleanup: {}", path, e);
        }
    }
    
    // ==================== 结果类 ====================
    
    public static class InstallResult {
        private final boolean success;
        private final SkillManifest manifest;
        private final Path installedPath;
        private final String errorMessage;
        
        private InstallResult(boolean success, SkillManifest manifest, 
                             Path installedPath, String errorMessage) {
            this.success = success;
            this.manifest = manifest;
            this.installedPath = installedPath;
            this.errorMessage = errorMessage;
        }
        
        public static InstallResult success(SkillManifest manifest, Path path) {
            return new InstallResult(true, manifest, path, null);
        }
        
        public static InstallResult failure(String errorMessage) {
            return new InstallResult(false, null, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public SkillManifest getManifest() { return manifest; }
        public Path getInstalledPath() { return installedPath; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class UninstallResult {
        private final boolean success;
        private final String skillCode;
        private final String errorMessage;
        
        private UninstallResult(boolean success, String skillCode, String errorMessage) {
            this.success = success;
            this.skillCode = skillCode;
            this.errorMessage = errorMessage;
        }
        
        public static UninstallResult success(String skillCode) {
            return new UninstallResult(true, skillCode, null);
        }
        
        public static UninstallResult failure(String errorMessage) {
            return new UninstallResult(false, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public String getSkillCode() { return skillCode; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class UpdateResult {
        private final boolean success;
        private final SkillManifest manifest;
        private final Path updatedPath;
        private final String errorMessage;
        
        private UpdateResult(boolean success, SkillManifest manifest, 
                            Path updatedPath, String errorMessage) {
            this.success = success;
            this.manifest = manifest;
            this.updatedPath = updatedPath;
            this.errorMessage = errorMessage;
        }
        
        public static UpdateResult success(SkillManifest manifest, Path path) {
            return new UpdateResult(true, manifest, path, null);
        }
        
        public static UpdateResult failure(String errorMessage) {
            return new UpdateResult(false, null, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public SkillManifest getManifest() { return manifest; }
        public Path getUpdatedPath() { return updatedPath; }
        public String getErrorMessage() { return errorMessage; }
    }
}
