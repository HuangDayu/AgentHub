package com.agenthub.application.usecase;

import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.application.port.out.tools.SkillToolScannerPort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.AgentConfig;
import com.agenthub.domain.model.skill.Skill;
import com.agenthub.domain.model.skill.SkillFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.agenthub.common.utils.TtlUtils.parallelStreamWithTtl;
import static com.agenthub.domain.enums.AgentConfigCategory.TOOL;
import static com.agenthub.domain.enums.AgentConfigType.MCP_TOOL;

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

    @Value("${agenthub.skills.share-path:${user.home}/.agents/skills}")
    private String skillSharePath;

    /**
     * 创建同步技能。
     */
    @Transactional
    public SkillOutput createSynced(String tenantId, String workspaceId,
                                     String skillCode, String name,
                                     String description, String skillPath) {
        Skill skill = Skill.createSynced(tenantId, workspaceId,
                skillCode, name, description, skillPath);
        skill.setSkillType("SYNCED");
        skill.setSource("LOCAL");
        skill.setSourcePath(skillPath);
        Skill saved = skillRepository.saveOrUpdate(skill);
        syncFilesToLocal(saved);
        return toOutput(saved);
    }

    /**
     * 从 URL 创建上传技能。
     */
    @Transactional
    public SkillOutput createFromUrl(String tenantId, String workspaceId,
                                      String skillCode, String name,
                                      String description, String zipUrl) {
        Skill skill = Skill.createFromUrl(tenantId, workspaceId,
                skillCode, name, description, zipUrl);
        skill.setSkillType("UPLOADED");
        skill.setSource("URL");
        skill.setSourcePath(zipUrl);
        Skill saved = skillRepository.saveOrUpdate(skill);
        processZipFromUrl(saved, zipUrl);
        return toOutput(saved);
    }

    /**
     * 从上传创建技能。
     */
    @Transactional
    public SkillOutput createFromUpload(String tenantId, String workspaceId,
                                         String skillCode, String name,
                                         String description,
                                         InputStream zipStream, long zipSize) {
        Skill skill = Skill.createFromUpload(tenantId, workspaceId,
                skillCode, name, description);
        skill.setSkillType("UPLOADED");
        skill.setSource("UPLOAD");
        Skill saved = skillRepository.saveOrUpdate(skill);
        processUploadedZip(saved, zipStream, zipSize);
        return toOutput(saved);
    }

    /**
     * 列出技能。
     */
    public List<SkillOutput> list() {
        return skillRepository.findAll().stream()
                .map(this::toOutput)
                .toList();
    }

    /**
     * 获取技能。
     */
    public SkillOutput get(String skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
        return toOutput(skill);
    }

    /**
     * 更新技能。
     */
    @Transactional
    public SkillOutput update(String skillId, String name, String description, String skillPath) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
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
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
        skill.enable();
        Skill saved = skillRepository.saveOrUpdate(skill);
        return toOutput(saved);
    }

    /**
     * 禁用技能。
     */
    @Transactional
    public SkillOutput disable(String skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
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
    @Transactional
    public void syncAll() {
        List<Skill> skills = skillToolScannerPort.scanSkills(skillSharePath);
        parallelStreamWithTtl(8, skills, skill -> {
            skill.setSkillType("SYNCED");
            skill.setSource("LOCAL");
            skill.setSourcePath(skillSharePath);
            Skill saved = skillRepository.saveOrUpdate(skill);
            syncFilesToLocal(saved);
            return null;
        });

    }

    /**
     * 同步文件到本地和 MinIO。
     */
    private void syncFilesToLocal(Skill skill) {
        List<SkillFile> files = scanAndStoreSkillFiles(skill);
        skillFileRepository.deleteBySkillId(skill.getId());
        skillFileRepository.saveAll(files);
        updateSkillStats(skill);
    }

    /**
     * 扫描并存储技能文件。
     */
    private List<SkillFile> scanAndStoreSkillFiles(Skill skill) {
        Path skillPath = Path.of(skill.getSkillPath());
        List<SkillFile> files = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(skillPath)) {
            paths.parallel().filter(Files::isRegularFile).forEach(path -> {
                processFile(skill, skillPath, path, files);
            });
        } catch (Exception e) {
            log.error("Failed to scan skill files: {}", skill.getSkillPath(), e);
        }
        return files;
    }

    /**
     * 处理单个文件。
     */
    private void processFile(Skill skill, Path skillPath, Path path, List<SkillFile> files) {
        try {
            String relativePath = skillPath.relativize(path).toString();
            long size = Files.size(path);
            uploadFileToMinio(skill.getSkillCode(), relativePath, path, size);
            SkillFile file = createSkillFile(skill, relativePath, size);
            files.add(file);
        } catch (Exception e) {
            log.error("Failed to process file: {}", path, e);
        }
    }

    /**
     * 上传文件到 MinIO。
     */
    private void uploadFileToMinio(String skillCode, String relativePath, Path path, long size) {
        String storagePath = buildStoragePath(skillCode, relativePath);
        try (InputStream content = Files.newInputStream(path)) {
            documentFileStoragePort.store(storagePath, content, size);
        } catch (Exception e) {
            log.warn("Failed to upload file to MinIO (MinIO may be unavailable): {}", relativePath);
        }
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
    private SkillFile createSkillFile(Skill skill, String relativePath, long size) {
        return SkillFile.create(skill.getId(), skill.getTenantId(),
                skill.getWorkspaceId(), relativePath, size, "UTF-8");
    }

    /**
     * 更新 skill 文件统计。
     */
    private void updateSkillStats(Skill skill) {
        SkillFileRepository.FileStats stats = skillFileRepository.getStats(skill.getId());
        skill.updateFileStats(stats.fileCount(), stats.totalSize());
        skill.markSynced();
        skillRepository.saveOrUpdate(skill);
    }

    /**
     * 处理 URL ZIP。
     */
    private void processZipFromUrl(Skill skill, String zipUrl) {
        try {
            Path tempDir = Files.createTempDirectory("skill-zip-");
            Path zipPath = tempDir.resolve("skill.zip");
            downloadZip(zipUrl, zipPath);
            saveZipToMinio(skill.getSkillCode(), zipPath);
            extractAndProcess(skill, zipPath);
            deleteDirectory(tempDir);
        } catch (Exception e) {
            log.error("Failed to process ZIP from URL: {}", zipUrl, e);
            throw new RuntimeException("Failed to process ZIP", e);
        }
    }

    /**
     * 处理上传的 ZIP。
     */
    private void processUploadedZip(Skill skill, InputStream zipStream, long zipSize) {
        try {
            Path tempDir = Files.createTempDirectory("skill-zip-");
            Path zipPath = tempDir.resolve("skill.zip");
            Files.copy(zipStream, zipPath);
            saveZipToMinio(skill.getSkillCode(), zipPath);
            extractAndProcess(skill, zipPath);
            deleteDirectory(tempDir);
        } catch (Exception e) {
            log.error("Failed to process uploaded ZIP", e);
            throw new RuntimeException("Failed to process ZIP", e);
        }
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
     * 解压并处理 ZIP。
     */
    private void extractAndProcess(Skill skill, Path zipPath) throws Exception {
        Path extractDir = Files.createTempDirectory("skill-extract-");
        unzip(zipPath, extractDir);
        List<SkillFile> files = scanExtractedFiles(skill, extractDir);
        skillFileRepository.saveAll(files);
        updateSkillStats(skill);
        deleteDirectory(extractDir);
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
     * 扫描解压后的文件。
     */
    private List<SkillFile> scanExtractedFiles(Skill skill, Path extractDir) {
        List<SkillFile> files = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(extractDir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                processExtractedFile(skill, extractDir, path, files);
            });
        } catch (Exception e) {
            log.error("Failed to scan extracted files", e);
        }
        return files;
    }

    /**
     * 处理解压后的单个文件。
     */
    private void processExtractedFile(Skill skill, Path extractDir, Path path, List<SkillFile> files) {
        try {
            String relativePath = extractDir.relativize(path).toString();
            long size = Files.size(path);
            uploadFileToMinio(skill.getSkillCode(), relativePath, path, size);
            SkillFile file = createSkillFile(skill, relativePath, size);
            files.add(file);
        } catch (Exception e) {
            log.error("Failed to process extracted file: {}", path, e);
        }
    }

    /**
     * 删除临时目录。
     */
    private void deleteDirectory(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); }
                        catch (Exception e) { log.warn("Failed to delete: {}", p); }
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
