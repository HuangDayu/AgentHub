package com.agenthub.infrastructure.etl;

import com.agenthub.application.port.out.etl.EtlDocumentChunkerPort;
import com.agenthub.domain.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档分块适配器，基于段落边界的智能文本分块。
 */
@Component
public class EtlDocumentChunkerAdapter implements EtlDocumentChunkerPort {

    private static final Logger log = LoggerFactory.getLogger(EtlDocumentChunkerAdapter.class);

    /**
     * 对文档内容进行分块。
     */
    @Override
    public List<DocumentChunk> chunk(String documentId, String kbId, String content, int chunkSize, int overlap) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return chunks;
        }
        List<String> paragraphs = splitByParagraphs(content);
        return processParagraphs(documentId, kbId, paragraphs, chunkSize, overlap);
    }

    /**
     * 处理段落列表生成分块。
     */
    private List<DocumentChunk> processParagraphs(String documentId, String kbId, List<String> paragraphs, int chunkSize, int overlap) {
        List<DocumentChunk> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int index = 0;
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            index = processParagraph(documentId, kbId, trimmed, chunkSize, overlap, chunks, currentChunk, index);
        }
        addFinalChunk(documentId, kbId, chunks, currentChunk, index);
        return chunks;
    }

    /**
     * 处理单个段落。
     */
    private int processParagraph(String documentId, String kbId, String trimmed, int chunkSize, int overlap,
                                 List<DocumentChunk> chunks, StringBuilder currentChunk, int index) {
        if (!currentChunk.isEmpty() && currentChunk.length() + trimmed.length() + 2 > chunkSize) {
            index = saveCurrentChunk(documentId, kbId, chunks, currentChunk, index, overlap);
        }
        if (trimmed.length() > chunkSize) {
            return processLargeParagraph(documentId, kbId, trimmed, chunkSize, overlap, chunks, currentChunk, index);
        }
        appendToCurrentChunk(currentChunk, trimmed);
        return index;
    }

    /**
     * 保存当前分块。
     */
    private int saveCurrentChunk(String documentId, String kbId, List<DocumentChunk> chunks,
                                 StringBuilder currentChunk, int index, int overlap) {
        String chunkText = currentChunk.toString().trim();
        if (!chunkText.isEmpty()) {
            chunks.add(DocumentChunk.create(documentId, kbId, index++, chunkText));
        }
        String overlapText = getOverlapText(chunkText, overlap);
        currentChunk.setLength(0);
        currentChunk.append(overlapText);
        return index;
    }

    /**
     * 处理大段落。
     */
    private int processLargeParagraph(String documentId, String kbId, String trimmed, int chunkSize, int overlap,
                                      List<DocumentChunk> chunks, StringBuilder currentChunk, int index) {
        List<String> subChunks = splitLargeParagraph(trimmed, chunkSize, overlap);
        for (String sub : subChunks) {
            index = processSubChunk(documentId, kbId, sub, chunkSize, chunks, currentChunk, index);
        }
        return index;
    }

    /**
     * 处理子分块。
     */
    private int processSubChunk(String documentId, String kbId, String sub, int chunkSize,
                                List<DocumentChunk> chunks, StringBuilder currentChunk, int index) {
        if (!currentChunk.isEmpty()) {
            index = saveCurrentChunkWithOverlap(documentId, kbId, chunks, currentChunk, index, chunkSize);
        }
        if (currentChunk.length() + sub.length() > chunkSize) {
            index = handleOversizedSub(documentId, kbId, sub, chunks, currentChunk, index);
        } else {
            appendToCurrentChunk(currentChunk, sub);
        }
        return index;
    }

    /**
     * 保存当前分块并设置重叠。
     */
    private int saveCurrentChunkWithOverlap(String documentId, String kbId, List<DocumentChunk> chunks,
                                            StringBuilder currentChunk, int index, int chunkSize) {
        chunks.add(DocumentChunk.create(documentId, kbId, index++, currentChunk.toString().trim()));
        String overlapText = getOverlapText(currentChunk.toString().trim(), chunkSize / 10);
        currentChunk.setLength(0);
        currentChunk.append(overlapText);
        return index;
    }

    /**
     * 处理超大子分块。
     */
    private int handleOversizedSub(String documentId, String kbId, String sub,
                                   List<DocumentChunk> chunks, StringBuilder currentChunk, int index) {
        if (!currentChunk.isEmpty()) {
            chunks.add(DocumentChunk.create(documentId, kbId, index++, currentChunk.toString().trim()));
        }
        currentChunk.setLength(0);
        currentChunk.append(sub);
        return index;
    }

    /**
     * 追加到当前分块。
     */
    private void appendToCurrentChunk(StringBuilder currentChunk, String text) {
        if (!currentChunk.isEmpty()) {
            currentChunk.append("\n\n");
        }
        currentChunk.append(text);
    }

    /**
     * 添加最后一个分块。
     */
    private void addFinalChunk(String documentId, String kbId, List<DocumentChunk> chunks,
                               StringBuilder currentChunk, int index) {
        if (!currentChunk.isEmpty()) {
            String chunkText = currentChunk.toString().trim();
            if (!chunkText.isEmpty()) {
                chunks.add(DocumentChunk.create(documentId, kbId, index, chunkText));
            }
        }
    }

    /**
     * 按双换行符分割段落。
     */
    private List<String> splitByParagraphs(String content) {
        List<String> paragraphs = new ArrayList<>();
        String[] parts = content.split("\\r?\\n\\s*\\r?\\n");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }
        return paragraphs;
    }

    /**
     * 拆分超过 chunkSize 的大段落。
     */
    private List<String> splitLargeParagraph(String paragraph, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < paragraph.length()) {
            int end = calculateChunkEnd(paragraph, start, chunkSize);
            var sub = paragraph.substring(start, end).trim();
            if (!sub.isEmpty()) result.add(sub);
            start = Math.max(start + 1, end - overlap);
        }
        return result;
    }

    private int calculateChunkEnd(String paragraph, int start, int chunkSize) {
        int end = Math.min(start + chunkSize, paragraph.length());
        if (end < paragraph.length()) {
            int sentenceEnd = findSentenceBoundary(paragraph, start, end);
            if (sentenceEnd > start) return sentenceEnd;
        }
        return end;
    }

    /**
     * 在指定范围内查找最近的句子边界。
     */
    private int findSentenceBoundary(String text, int start, int end) {
        for (int i = end - 1; i > start; i--) {
            char c = text.charAt(i);
            if (isSentenceEnd(c) && (i + 1 >= text.length() || Character.isWhitespace(text.charAt(i + 1)))) {
                return i + 1;
            }
        }
        return findWhitespaceBoundary(text, start, end);
    }

    /**
     * 判断是否为句子结束标点。
     */
    private boolean isSentenceEnd(char c) {
        return c == '.' || c == '。' || c == '!' || c == '！' || c == '?' || c == '？';
    }

    /**
     * 查找空白边界。
     */
    private int findWhitespaceBoundary(String text, int start, int end) {
        for (int i = end - 1; i > start; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return end;
    }

    /**
     * 获取文本尾部指定长度的重叠内容。
     */
    private String getOverlapText(String text, int overlap) {
        if (text == null || text.isEmpty() || overlap <= 0) {
            return "";
        }
        if (text.length() <= overlap) {
            return text;
        }
        return text.substring(text.length() - overlap);
    }
}
