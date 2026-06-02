package com.agenthub.application.usecase;

import com.agenthub.application.dto.SkillFileOutput;
import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.domain.model.skill.SkillFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 技能文件用例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillFileUseCase {

    private final SkillFileRepository skillFileRepository;
    private final DocumentFileStoragePort documentFileStoragePort;

    /**
     * 获取技能文件列表。
     */
    public List<SkillFileOutput> getSkillFiles(String skillId) {
        return skillFileRepository.findBySkillId(skillId).stream()
                .map(this::toOutput)
                .toList();
    }

    /**
     * 获取文件元数据。
     */
    public Optional<SkillFileOutput> getFile(String skillId, String filePath) {
        return skillFileRepository.findBySkillIdAndFilePath(skillId, filePath)
                .map(this::toOutput);
    }

    /**
     * 获取文件内容。
     */
    public InputStream getFileContent(String skillId, String filePath) {
        SkillFile file = skillFileRepository.findBySkillIdAndFilePath(skillId, filePath)
                .orElseThrow(() -> new RuntimeException("File not found: " + filePath));
        return documentFileStoragePort.retrieve(file.getStoragePath());
    }

    /**
     * 删除文件。
     */
    public void deleteFile(String skillId, String filePath) {
        SkillFile file = skillFileRepository.findBySkillIdAndFilePath(skillId, filePath)
                .orElseThrow(() -> new RuntimeException("File not found: " + filePath));
        documentFileStoragePort.delete(file.getStoragePath());
        skillFileRepository.deleteBySkillIdAndFilePath(skillId, filePath);
    }

    /**
     * 按扩展名查找文件。
     */
    public List<SkillFileOutput> getFilesByExt(String skillId, String ext) {
        return skillFileRepository.findBySkillIdAndFileExt(skillId, ext).stream()
                .map(this::toOutput)
                .toList();
    }

    /**
     * 获取文件统计。
     */
    public SkillFileRepository.FileStats getStats(String skillId) {
        return skillFileRepository.getStats(skillId);
    }

    /**
     * 转换为输出 DTO。
     */
    private SkillFileOutput toOutput(SkillFile file) {
        SkillFileOutput output = new SkillFileOutput();
        output.setId(file.getId());
        output.setSkillId(file.getSkillId());
        output.setTenantId(file.getTenantId());
        output.setWorkspaceId(file.getWorkspaceId());
        output.setFilePath(file.getFilePath());
        output.setFileName(file.getFileName());
        output.setFileExt(file.getFileExt());
        output.setFileSize(file.getFileSize());
        output.setFileType(file.getFileType() != null ? file.getFileType().name() : null);
        output.setEncoding(file.getEncoding());
        output.setStoragePath(file.getStoragePath());
        output.setChecksum(file.getChecksum());
        output.setDirectory(file.isDirectory());
        output.setMetadata(file.getMetadata());
        output.setVersion(file.getVersion());
        output.setCreatedAt(file.getCreatedAt());
        output.setUpdatedAt(file.getUpdatedAt());
        return output;
    }
}
