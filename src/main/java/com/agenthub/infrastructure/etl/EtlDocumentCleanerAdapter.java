package com.agenthub.infrastructure.etl;

import com.agenthub.application.port.out.etl.EtlDocumentCleanerPort;
import com.agenthub.domain.model.DocumentContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 内容清洗适配器，对文档文本进行标准化和噪声清理。
 * <p>
 * 清洗操作包括：
 * <ul>
 *   <li>移除控制字符等特殊字符</li>
 *   <li>移除多余空白和连续换行</li>
 *   <li>统一引号（中文引号 → 英文引号）</li>
 *   <li>统一破折号（各种破折号 → 标准连字符）</li>
 *   <li>清理 HTML 残留标签</li>
 * </ul>
 * </p>
 */
@Component
public class EtlDocumentCleanerAdapter implements EtlDocumentCleanerPort {

    private static final Logger log = LoggerFactory.getLogger(EtlDocumentCleanerAdapter.class);

    /** 控制字符模式（保留换行符 \n 和制表符 \t） */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\n\t]]");

    /** 连续空白字符（不含换行符） */
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ \\t]{2,}");

    /** 连续换行符（超过2个换行合并为2个） */
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{3,}");

    /** HTML 标签 */
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");

    /** HTML 实体 */
    private static final Pattern HTML_ENTITIES = Pattern.compile("&[a-zA-Z]+;|&#\\d+;");

    private static final Map<String, String> HTML_ENTITY_MAP = Map.of(
        "&nbsp;", " ", "&amp;", "&", "&lt;", "<",
        "&gt;", ">", "&quot;", "\"", "&apos;", "'", "&#39;", "'"
    );

    /**
     * 清洗文档内容。
     *
     * @param content 文档内容
     * @return 清洗后的文档内容
     */
    @Override
    public DocumentContent clean(DocumentContent content) {
        String raw = content.getRawContent();
        if (raw == null || raw.isBlank()) {
            return content.withCleanedContent("");
        }
        String cleaned = applyCleaningSteps(raw);
        return content.withCleanedContent(cleaned);
    }

    /**
     * 应用清洗步骤。
     */
    private String applyCleaningSteps(String raw) {
        String cleaned = raw;
        cleaned = removeHtmlTags(cleaned);
        cleaned = removeControlChars(cleaned);
        cleaned = normalizeQuotes(cleaned);
        cleaned = normalizeDashes(cleaned);
        cleaned = normalizeWhitespace(cleaned);
        return cleaned;
    }

    /**
     * 移除 HTML 残留标签和常见 HTML 实体。
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String removeHtmlTags(String text) {
        String result = replaceHtmlEntities(text);
        return HTML_TAGS.matcher(result).replaceAll("");
    }

    /**
     * 替换HTML实体。
     */
    private String replaceHtmlEntities(String text) {
        return HTML_ENTITIES.matcher(text).replaceAll(match ->
                HTML_ENTITY_MAP.getOrDefault(match.group(), ""));
    }

    /**
     * 移除控制字符（保留换行符和制表符）。
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String removeControlChars(String text) {
        return CONTROL_CHARS.matcher(text).replaceAll("");
    }

    /**
     * 统一引号：将中文引号、弯引号等统一为标准 ASCII 引号。
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String normalizeQuotes(String text) {
        return text
                .replace('\u201C', '"')  // 左双引号 "
                .replace('\u201D', '"')  // 右双引号 "
                .replace('\u2018', '\'') // 左单引号 '
                .replace('\u2019', '\'') // 右单引号 '
                .replace('\u300C', '"')  // 日文左引号 「
                .replace('\u300D', '"')  // 日文右引号 」
                .replace('\u300E', '"')  // 日文左双引号 『
                .replace('\u300F', '"'); // 日文右双引号 』
    }

    /**
     * 统一破折号：将各种破折号和连字符统一为标准 ASCII 连字符。
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String normalizeDashes(String text) {
        return text
                .replace('\u2014', '-')  // em dash —
                .replace('\u2013', '-')  // en dash –
                .replace('\u2015', '-')  // horizontal bar ―
                .replace('\u2010', '-')  // hyphen ‐
                .replace('\uFF0D', '-')  // fullwidth hyphen －
                .replace("——", " - ");   // 中文双破折号
    }

    /**
     * 规范化空白字符。
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String normalizeWhitespace(String text) {
        String result = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        result = MULTIPLE_NEWLINES.matcher(result).replaceAll("\n\n");
        return trimLines(result);
    }

    /**
     * 去除每行首尾空白。
     */
    private String trimLines(String text) {
        String[] lines = text.split("\\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(lines[i].trim());
        }
        return sb.toString().trim();
    }
}
