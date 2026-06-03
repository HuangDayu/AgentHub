package com.agenthub.application.usecase;

import com.agenthub.application.dto.SkillFileOutput;
import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.domain.model.skill.SkillFile;
import com.agenthub.domain.model.skill.SkillFileStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 技能文件用例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillFileUseCase {

    private static final Tika TIKA = new Tika();
    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "ico", "webp",
            "mp3", "mp4", "avi", "mov", "wav", "flac",
            "zip", "tar", "gz", "rar", "7z",
            "exe", "dll", "so", "class",
            "woff", "woff2", "ttf", "otf", "eot"
    );

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
    public Optional<SkillFileOutput> getFile(String skillId, String fileId) {
        return skillFileRepository.findBySkillIdAndFileId(skillId, fileId)
                .map(this::toOutput);
    }

    /**
     * 获取文件文本内容。二进制文件返回提示信息。
     */
    public String getFileContent(String skillId, String fileId) {
        SkillFile file = skillFileRepository.findBySkillIdAndFileId(skillId, fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        String ext = file.getFileExt() != null ? file.getFileExt().toLowerCase() : "";
        if (BINARY_EXTENSIONS.contains(ext)) {
            return "[二进制文件: " + file.getFileName() + "]";
        }

        try (InputStream is = documentFileStoragePort.retrieve(file.getStoragePath())) {
            return TIKA.parseToString(is);
        } catch (Exception e) {
            log.warn("读取文件内容失败: {}", file.getStoragePath(), e);
            return "[读取失败: " + e.getMessage() + "]";
        }
    }

    /**
     * 删除文件。
     */
    public void deleteFile(String skillId, String fileId) {
        SkillFile file = skillFileRepository.findBySkillIdAndFileId(skillId, fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));
        documentFileStoragePort.delete(file.getStoragePath());
        skillFileRepository.deleteBySkillIdAndFileId(skillId, fileId);
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
    public SkillFileStats getStats(String skillId) {
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
