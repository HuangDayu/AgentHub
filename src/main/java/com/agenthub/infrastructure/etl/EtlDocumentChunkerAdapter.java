package com.agenthub.infrastructure.etl;

import com.agenthub.application.port.out.etl.ChunkSpec;
import com.agenthub.application.port.out.etl.EtlDocumentChunkerPort;
import com.agenthub.domain.model.etl.DocumentChunk;
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
     * 分块过程中的可变上下文，避免在内部方法间传递大量共享参数。
     */
    private static final class ChunkContext {
        private final String documentId;
        private final String kbId;
        private final int chunkSize;
        private final int overlap;
        private final List<DocumentChunk> chunks = new ArrayList<>();
        private final StringBuilder currentChunk = new StringBuilder();
        private int index;

        ChunkContext(String documentId, String kbId, int chunkSize, int overlap) {
            this.documentId = documentId;
            this.kbId = kbId;
            this.chunkSize = chunkSize;
            this.overlap = overlap;
        }
    }

    /**
     * 对文档内容进行分块。
     */
    @Override
    public List<DocumentChunk> chunk(ChunkSpec spec) {
        String content = spec.getContent();
        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }
        ChunkContext ctx = new ChunkContext(spec.getDocumentId(), spec.getKbId(),
                spec.getChunkSize(), spec.getOverlap());
        processParagraphs(ctx, splitByParagraphs(content));
        return ctx.chunks;
    }

    /**
     * 处理段落列表生成分块。
     */
    private void processParagraphs(ChunkContext ctx, List<String> paragraphs) {
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            ctx.index = processParagraph(ctx, trimmed);
        }
        addFinalChunk(ctx);
    }

    /**
     * 处理单个段落。
     */
    private int processParagraph(ChunkContext ctx, String trimmed) {
        if (!ctx.currentChunk.isEmpty() && ctx.currentChunk.length() + trimmed.length() + 2 > ctx.chunkSize) {
            ctx.index = saveCurrentChunk(ctx);
        }
        if (trimmed.length() > ctx.chunkSize) {
            return processLargeParagraph(ctx, trimmed);
        }
        appendToCurrentChunk(ctx.currentChunk, trimmed);
        return ctx.index;
    }

    /**
     * 保存当前分块。
     */
    private int saveCurrentChunk(ChunkContext ctx) {
        String chunkText = ctx.currentChunk.toString().trim();
        if (!chunkText.isEmpty()) {
            ctx.chunks.add(DocumentChunk.create(ctx.documentId, ctx.kbId, ctx.index++, chunkText));
        }
        String overlapText = getOverlapText(chunkText, ctx.overlap);
        ctx.currentChunk.setLength(0);
        ctx.currentChunk.append(overlapText);
        return ctx.index;
    }

    /**
     * 处理大段落。
     */
    private int processLargeParagraph(ChunkContext ctx, String trimmed) {
        for (String sub : splitLargeParagraph(trimmed, ctx.chunkSize, ctx.overlap)) {
            ctx.index = processSubChunk(ctx, sub);
        }
        return ctx.index;
    }

    /**
     * 处理子分块。
     */
    private int processSubChunk(ChunkContext ctx, String sub) {
        if (!ctx.currentChunk.isEmpty()) {
            ctx.index = saveCurrentChunkWithOverlap(ctx);
        }
        if (ctx.currentChunk.length() + sub.length() > ctx.chunkSize) {
            ctx.index = handleOversizedSub(ctx, sub);
        } else {
            appendToCurrentChunk(ctx.currentChunk, sub);
        }
        return ctx.index;
    }

    /**
     * 保存当前分块并设置重叠。
     */
    private int saveCurrentChunkWithOverlap(ChunkContext ctx) {
        ctx.chunks.add(DocumentChunk.create(ctx.documentId, ctx.kbId, ctx.index++, ctx.currentChunk.toString().trim()));
        String overlapText = getOverlapText(ctx.currentChunk.toString().trim(), ctx.chunkSize / 10);
        ctx.currentChunk.setLength(0);
        ctx.currentChunk.append(overlapText);
        return ctx.index;
    }

    /**
     * 处理超大子分块。
     */
    private int handleOversizedSub(ChunkContext ctx, String sub) {
        if (!ctx.currentChunk.isEmpty()) {
            ctx.chunks.add(DocumentChunk.create(ctx.documentId, ctx.kbId, ctx.index++, ctx.currentChunk.toString().trim()));
        }
        ctx.currentChunk.setLength(0);
        ctx.currentChunk.append(sub);
        return ctx.index;
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
    private void addFinalChunk(ChunkContext ctx) {
        if (ctx.currentChunk.isEmpty()) {
            return;
        }
        String chunkText = ctx.currentChunk.toString().trim();
        if (!chunkText.isEmpty()) {
            ctx.chunks.add(DocumentChunk.create(ctx.documentId, ctx.kbId, ctx.index, chunkText));
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
