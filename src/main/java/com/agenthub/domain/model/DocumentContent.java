package com.agenthub.domain.model;

import java.util.Objects;

/**
 * 文档内容领域模型，不可变对象。
 * <p>
 * 封装文档的原始内容和清洗后的内容，支持多种文档格式的检测。
 * </p>
 */
public final class DocumentContent {
    /** 文档唯一标识 */
    private final String documentId;
    /** 文档原始内容 */
    private final String rawContent;
    /** 清洗后的文档内容 */
    private final String cleanedContent;
    /** 文档格式类型 */
    private final DocumentFormat format;

    private DocumentContent(String documentId, String rawContent, String cleanedContent, DocumentFormat format) {
        this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
        this.rawContent = rawContent == null ? "" : rawContent;
        this.cleanedContent = cleanedContent == null ? "" : cleanedContent;
        this.format = format == null ? DocumentFormat.UNKNOWN : format;
    }

    /**
     * 创建文档内容实例。
     *
     * @param documentId 文档ID
     * @param rawContent 原始内容
     * @param format     文档格式
     * @return 新的DocumentContent实例
     */
    public static DocumentContent create(String documentId, String rawContent, DocumentFormat format) {
        return new DocumentContent(documentId, rawContent, null, format);
    }

    /**
     * 从持久化数据重建文档内容实例。
     *
     * @param documentId    文档ID
     * @param rawContent    原始内容
     * @param cleanedContent 清洗后内容
     * @param format        文档格式
     * @return 重建的DocumentContent实例
     */
    public static DocumentContent reconstruct(String documentId, String rawContent, String cleanedContent, DocumentFormat format) {
        return new DocumentContent(documentId, rawContent, cleanedContent, format);
    }

    /**
     * 根据内容类型和文件名检测文档格式。
     * <p>
     * 优先根据contentType判断，若无法确定则回退到文件名后缀。
     * </p>
     *
     * @param contentType MIME内容类型
     * @param fileName    文件名
     * @return 检测到的文档格式
     */
    public static DocumentFormat detectFormat(String contentType, String fileName) {
        if (contentType == null && fileName == null) {
            return DocumentFormat.UNKNOWN;
        }

        if (contentType != null) {
            return switch (contentType.toLowerCase()) {
                case "application/pdf" -> DocumentFormat.PDF;
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                     "application/msword" -> DocumentFormat.WORD;
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                     "application/vnd.ms-excel" -> DocumentFormat.EXCEL;
                case "text/plain" -> DocumentFormat.TEXT;
                case "text/markdown" -> DocumentFormat.MARKDOWN;
                case "text/html" -> DocumentFormat.HTML;
                default -> detectFormatByFileName(fileName);
            };
        }

        return detectFormatByFileName(fileName);
    }

    private static DocumentFormat detectFormatByFileName(String fileName) {
        if (fileName == null) {
            return DocumentFormat.UNKNOWN;
        }

        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return DocumentFormat.PDF;
        }
        if (lowerName.endsWith(".docx") || lowerName.endsWith(".doc")) {
            return DocumentFormat.WORD;
        }
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            return DocumentFormat.EXCEL;
        }
        if (lowerName.endsWith(".txt")) {
            return DocumentFormat.TEXT;
        }
        if (lowerName.endsWith(".md")) {
            return DocumentFormat.MARKDOWN;
        }
        if (lowerName.endsWith(".json")) {
            return DocumentFormat.JSON;
        }
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return DocumentFormat.HTML;
        }
        return DocumentFormat.UNKNOWN;
    }

    /**
     * 返回带有清洗后内容的新实例。
     *
     * @param cleanedContent 清洗后的内容
     * @return 包含清洗内容的新DocumentContent实例
     */
    public DocumentContent withCleanedContent(String cleanedContent) {
        return new DocumentContent(documentId, rawContent, cleanedContent, format);
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getCleanedContent() {
        return cleanedContent;
    }

    public DocumentFormat getFormat() {
        return format;
    }

    /**
     * 文档格式枚举，标识文档的类型。
     */
    public enum DocumentFormat {
        PDF,
        WORD,
        EXCEL,
        TEXT,
        MARKDOWN,
        JSON,
        HTML,
        UNKNOWN
    }
}
