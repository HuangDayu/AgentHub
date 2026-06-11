package com.agenthub.domain.model.skill;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 技能文件元数据。
 */
public class SkillFile {
    private String id;
    private String skillId;
    private String tenantId;
    private String workspaceId;
    private String filePath;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private FileType fileType;
    private String encoding;
    private String storagePath;
    private String checksum;
    private boolean isDirectory;
    private String metadata;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 文件类型枚举。
     */
    public enum FileType {
        TEXT, BINARY, JSON, YAML, MD, IMAGE, UNKNOWN
    }

    /**
     * 创建文件元数据所需字段快照。
     */
    public static final class CreationSpec {
        private final String skillId;
        private final String tenantId;
        private final String workspaceId;
        private final String filePath;
        private final long fileSize;
        private final String encoding;
        private final String skillCode;
        private final String storagePath;

        public CreationSpec(String skillId, String tenantId, String workspaceId,
                                String filePath, long fileSize, String encoding,
                                String skillCode, String storagePath) {
            this.skillId = skillId;
            this.tenantId = tenantId;
            this.workspaceId = workspaceId;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.encoding = encoding;
            this.skillCode = skillCode;
            this.storagePath = storagePath;
        }
    }

    /**
     * 创建文件元数据。
     */
    public static SkillFile create(CreationSpec spec) {
        SkillFile file = new SkillFile();
        applySpecToFile(file, spec);
        initFileMetadata(file, spec);
        return file;
    }

    private static void initFileMetadata(SkillFile file, CreationSpec spec) {
        Instant now = Instant.now();
        file.id = randomId();
        file.fileName = extractFileName(spec.filePath);
        file.fileExt = extractExtension(spec.filePath);
        file.fileType = detectFileType(file.fileExt);
        file.version = 1;
        file.createdAt = now;
        file.updatedAt = now;
    }

    private static void applySpecToFile(SkillFile file, CreationSpec spec) {
        file.skillId = spec.skillId;
        file.tenantId = spec.tenantId;
        file.workspaceId = spec.workspaceId;
        file.filePath = spec.filePath;
        file.fileSize = spec.fileSize;
        file.encoding = spec.encoding;
        file.storagePath = spec.storagePath;
    }

    /**
     * 提取文件名。
     */
    private static String extractFileName(String filePath) {
        if (filePath == null) return "";
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * 提取扩展名。
     */
    private static String extractExtension(String filePath) {
        String fileName = extractFileName(filePath);
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot).toLowerCase() : "";
    }

    /**
     * 检测文件类型。
     */
    private static FileType detectFileType(String ext) {
        if (ext == null) return FileType.UNKNOWN;
        return switch (ext.toLowerCase()) {
            case ".md", ".txt", ".java", ".py", ".js", ".ts", ".html", ".css", ".xml" -> FileType.TEXT;
            case ".json" -> FileType.JSON;
            case ".yaml", ".yml" -> FileType.YAML;
            case ".jpg", ".jpeg", ".png", ".gif", ".svg", ".webp" -> FileType.IMAGE;
            default -> FileType.UNKNOWN;
        };
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileExt() { return fileExt; }
    public void setFileExt(String fileExt) { this.fileExt = fileExt; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public FileType getFileType() { return fileType; }
    public void setFileType(FileType fileType) { this.fileType = fileType; }
    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
