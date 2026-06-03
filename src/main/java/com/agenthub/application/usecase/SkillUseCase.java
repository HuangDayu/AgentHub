package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.agenthub.application.command.CreateSkillCommand;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.SkillConfigRepository;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.application.port.out.tools.SkillToolScannerPort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.skill.Skill;
import com.agenthub.domain.model.skill.SkillConfig;
import com.agenthub.domain.model.skill.SkillFile;
import com.agenthub.domain.model.skill.SkillFileStats;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.agenthub.common.utils.TtlUtils.parallelStreamWithTtl;

/**
 * 技能用例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillUseCase {

    private final SkillRepository skillRepository;
    private final SkillFileRepository skillFileRepository;
    private final SkillToolScannerPort skillToolScannerPort;
    private final DocumentFileStoragePort documentFileStoragePort;
    private final SkillConfigRepository skillConfigRepository;

    @Value("${agenthub.skills.share-path:${user.home}/.agents/skills}")
    private String skillSharePath;

    /**
     * 创建同步技能。
     */
    @Transactional
    public SkillOutput createSynced(CreateSkillCommand createSkillCommand) {
        Skill skill = scanSkill(createSkillCommand, Path.of(createSkillCommand.getSkillPath()));
        Skill saved = skillRepository.saveOrUpdate(skill);
        syncFilesToLocal(saved, Path.of(createSkillCommand.getSkillPath()));
        return toOutput(saved);
    }

    /**
     * 从路径提取技能编码。
     */
    private String extractSkillCode(String skillPath) {
        if (skillPath == null || skillPath.isBlank()) {
            return "skill-" + System.currentTimeMillis();
        }
        String normalized = skillPath.replace("\\", "/");
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            return normalized.substring(lastSlash + 1);
        }
        return normalized;
    }

    /**
     * 从 URL 创建上传技能。
     */
    @SneakyThrows
    @Transactional
    public SkillOutput createFromUrl(CreateSkillCommand createSkillCommand) {
        Path tempDir = Files.createTempDirectory("skill-zip-");
        Path zipPath = tempDir.resolve("skill.zip");
        downloadZip(createSkillCommand.getZipUrl(), zipPath);
        Skill saved = extractAndProcess(createSkillCommand, zipPath);
        saveZipToMinio(saved.getSkillCode(), zipPath);
        deleteDirectory(tempDir);
        return toOutput(saved);
    }

    /**
     * 从 URL 提取技能编码。
     */
    private String extractSkillCodeFromUrl(String zipUrl) {
        if (zipUrl == null || zipUrl.isBlank()) {
            return "skill-" + System.currentTimeMillis();
        }
        String normalized = zipUrl.split("\\?")[0];
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        int lastSlash = normalized.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        if (fileName.endsWith(".zip")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }

    /**
     * 从上传创建技能。
     */
    @SneakyThrows
    @Transactional
    public SkillOutput createFromUpload(CreateSkillCommand createSkillCommand) {
        Path tempDir = Files.createTempDirectory("skill-zip-");
        Path zipPath = tempDir.resolve("skill.zip");
        Files.copy(createSkillCommand.getZipStream(), zipPath);
        Skill saved = extractAndProcess(createSkillCommand, zipPath);
        saveZipToMinio(saved.getSkillCode(), zipPath);
        deleteDirectory(tempDir);
        return toOutput(saved);
    }

    /**
     * 列出技能。
     */
    public List<SkillOutput> list() {
        return skillRepository.findAll().stream().map(this::toOutput).toList();
    }

    /**
     * 获取技能。
     */
    public SkillOutput get(String skillId) {
        Skill skill = skillRepository.findById(skillId).orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
        return toOutput(skill);
    }

    /**
     * 更新技能。
     */
    @Transactional
    public SkillOutput update(String skillId, String name, String description, String skillPath) {
        Skill skill = skillRepository.findById(skillId).orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
        skill.setName(name);
        skill.setDescription(description);
        skill.setSkillPath(skillPath);
        Skill saved = skillRepository.saveOrUpdate(skill);
        return toOutput(saved);
    }

    /**
     * 启用技能。
     */
    @Transactional
    public SkillOutput enable(String skillId) {
        Skill skill = skillRepository.findById(skillId).orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
        skill.enable();
        Skill saved = skillRepository.saveOrUpdate(skill);
        return toOutput(saved);
    }

    /**
     * 禁用技能。
     */
    @Transactional
    public SkillOutput disable(String skillId) {
        Skill skill = skillRepository.findById(skillId).orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
        skill.disable();
        Skill saved = skillRepository.saveOrUpdate(skill);
        return toOutput(saved);
    }

    /**
     * 删除技能。
     */
    @Transactional
    public void delete(String skillId) {
        skillRepository.deleteById(skillId);
    }

    /**
     * 同步所有技能。
     */
    public void syncAll() {
        sync(skillSharePath);
    }

    public void sync(String skillPath) {
        List<Skill> skills = skillToolScannerPort.scanSkills(skillPath);
        parallelStreamWithTtl(8, skills, skill -> {
            skill.setSkillType("SYNCED");
            skill.setSource("LOCAL");
            skill.setSourcePath(skillPath);
            Skill saved = skillRepository.saveOrUpdate(skill);
            syncFilesToLocal(saved, Path.of(skill.getSkillPath()));
            return null;
        });
    }

    /**
     * 按配置同步技能。
     */
    @Transactional
    public void syncWithConfig(String configId) {
        SkillConfig config = skillConfigRepository.findById(configId).orElseThrow(() -> new NotFoundException("Skill config not found: " + configId));
        for (String path : config.getSkillPaths()) {
            List<Skill> skills = skillToolScannerPort.scanSkills(path);
            for (Skill skill : skills) {
                skill.setSkillType("SYNCED");
                skill.setSource("LOCAL");
                skill.setSourcePath(path);
                skill.setConfigId(configId);
                Skill saved = skillRepository.saveOrUpdate(skill);
                syncFilesToLocal(saved, Path.of(skill.getSkillPath()));
            }
        }
    }

    /**
     * 同步文件到本地和 MinIO。
     */
    private void syncFilesToLocal(Skill skill, Path skillPath) {
        List<SkillFile> files = scanAndStoreSkillFiles(skill, skillPath);
        skillFileRepository.deleteBySkillId(skill.getId());
        skillFileRepository.saveAll(files);
        updateSkillStats(skill);
    }

    /**
     * 扫描并存储技能文件。
     */
    private List<SkillFile> scanAndStoreSkillFiles(Skill skill, Path skillPath) {
        try (Stream<Path> paths = Files.walk(skillPath)) {
            return paths.parallel().filter(Files::isRegularFile).map(path -> processFile(skill, skillPath, path)).toList();
        } catch (Exception e) {
            log.error("Failed to scan skill files: {}", skill.getSkillPath(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 处理单个文件。
     */
    private SkillFile processFile(Skill skill, Path skillPath, Path path) {
        try {
            String relativePath = skillPath.relativize(path).toString();
            long size = Files.size(path);
            String storagePath = uploadFileToMinio(skill.getSkillCode(), relativePath, path, size);
            return createSkillFile(skill, relativePath, size, storagePath);
        } catch (Exception e) {
            log.error("Failed to process file: {}", path, e);
            return null;
        }
    }

    /**
     * 上传文件到 MinIO。
     */
    private String uploadFileToMinio(String skillCode, String relativePath, Path path, long size) {
        String storagePath = buildStoragePath(skillCode, relativePath);
        try (InputStream content = Files.newInputStream(path)) {
            documentFileStoragePort.store(storagePath, content, size);
        } catch (Exception e) {
            log.warn("Failed to upload file to MinIO (MinIO may be unavailable): {}", relativePath);
        }
        return storagePath;
    }

    /**
     * 构建存储路径。
     */
    private String buildStoragePath(String skillCode, String relativePath) {
        return String.format("skills/%s/%s", skillCode, relativePath);
    }

    /**
     * 创建 SkillFile 对象。
     */
    private SkillFile createSkillFile(Skill skill, String relativePath, long size, String storagePath) {
        return SkillFile.create(skill.getId(), skill.getTenantId(), skill.getWorkspaceId(), relativePath, size, "UTF-8", skill.getSkillCode(), storagePath);
    }

    /**
     * 更新 skill 文件统计。
     */
    private void updateSkillStats(Skill skill) {
        SkillFileStats stats = skillFileRepository.getStats(skill.getId());
        skill.updateFileStats(stats.getFileCount(), stats.getTotalSize());
        skill.markSynced();
        skillRepository.updateById(skill);
    }

    /**
     * 下载 ZIP 文件。
     */
    private void downloadZip(String zipUrl, Path zipPath) throws Exception {
        try (InputStream in = new java.net.URL(zipUrl).openStream()) {
            Files.copy(in, zipPath);
        }
    }

    /**
     * 保存 ZIP 到 MinIO。
     */
    private void saveZipToMinio(String skillCode, Path zipPath) throws Exception {
        String storagePath = buildStoragePath(skillCode, "_package.zip");
        try (InputStream zipIn = Files.newInputStream(zipPath)) {
            documentFileStoragePort.store(storagePath, zipIn, Files.size(zipPath));
        }
    }

    /**
     * 保存 ZIP 流到 MinIO。
     */
    private void saveZipToMinio(String skillCode, InputStream zipStream, long zipSize) {
        String storagePath = buildStoragePath(skillCode, "_package.zip");
        documentFileStoragePort.store(storagePath, zipStream, zipSize);
    }

    /**
     * 解压，扫描，保存，删除缓存。
     */
    private Skill extractAndProcess(CreateSkillCommand createSkillCommand, Path zipPath) throws Exception {
        Path extractDir = Files.createTempDirectory("skill-extract-");
        unzip(zipPath, extractDir);
        Skill skill = scanSkill(createSkillCommand, extractDir);
        Skill saved = skillRepository.saveOrUpdate(skill);
        syncFilesToLocal(saved, extractDir);
        deleteDirectory(extractDir);
        return saved;
    }


    private Skill scanSkill(CreateSkillCommand createSkillCommand, Path skillPath) {
        Optional<Skill> optional = skillToolScannerPort.loadSkillFromPath(skillPath);
        if (optional.isEmpty()) throw new NotFoundException("Skill not found: " + skillPath);
        Skill skill = optional.get();
        BeanUtil.copyProperties(createSkillCommand, skill,
                CopyOptions.create().ignoreNullValue().setPropertiesFilter((field, o) -> {
                    Object fieldValue = ReflectUtil.getFieldValue(createSkillCommand, field);
                    if (fieldValue instanceof String v) {
                        return StrUtil.isNotBlank(v);
                    }
                    return fieldValue != null;
                }));
        return skill;
    }

    /**
     * 解压 ZIP 文件。
     */
    private void unzip(Path zipPath, Path extractDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                extractEntry(extractDir, zis, entry);
                zis.closeEntry();
            }
        }
    }

    /**
     * 解压单个条目。
     */
    private void extractEntry(Path extractDir, ZipInputStream zis, ZipEntry entry) throws Exception {
        Path entryPath = extractDir.resolve(entry.getName());
        if (entry.isDirectory()) {
            Files.createDirectories(entryPath);
        } else {
            Files.createDirectories(entryPath.getParent());
            Files.copy(zis, entryPath);
        }
    }

    /**
     * 删除临时目录。
     */
    private void deleteDirectory(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (Exception e) {
                    log.warn("Failed to delete: {}", p);
                }
            });
        } catch (Exception e) {
            log.warn("Failed to delete directory: {}", dir);
        }
    }

    /**
     * 转换为输出 DTO。
     */
    private SkillOutput toOutput(Skill skill) {
        SkillOutput output = new SkillOutput();
        output.setId(skill.getId());
        output.setTenantId(skill.getTenantId());
        output.setWorkspaceId(skill.getWorkspaceId());
        output.setSkillCode(skill.getSkillCode());
        output.setName(skill.getName());
        output.setDescription(skill.getDescription());
        output.setSkillType(skill.getSkillType());
        output.setSkillPath(skill.getSkillPath());
        output.setSkillFilesTree(skill.getSkillFilesTree());
        output.setSource(skill.getSource());
        output.setSourcePath(skill.getSourcePath());
        output.setZipStoragePath(skill.getZipStoragePath());
        output.setConfigId(skill.getConfigId());
        output.setFileCount(skill.getFileCount());
        output.setTotalSize(skill.getTotalSize());
        output.setEnabled(skill.isEnabled());
        output.setCreatedAt(skill.getCreatedAt());
        output.setUpdatedAt(skill.getUpdatedAt());
        output.setLastSyncAt(skill.getLastSyncAt());
        return output;
    }
}
