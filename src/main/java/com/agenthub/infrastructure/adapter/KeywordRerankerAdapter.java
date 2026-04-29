package com.agenthub.infrastructure.adapter;

import com.agenthub.application.port.out.rag.RerankerPort;
import com.agenthub.domain.model.RetrievalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于关键词匹配的重排序适配器。
 * <p>
 * 使用关键词覆盖率和词频统计对检索结果进行重排序。
 * </p>
 */
@Component
public class KeywordRerankerAdapter implements RerankerPort {

    private static final Logger log = LoggerFactory.getLogger(KeywordRerankerAdapter.class);

    /** 原始向量分数权重 */
    private static final double ALPHA = 0.6;

    /** 关键词分数权重 */
    private static final double BETA = 1.0 - ALPHA;

    /** 停用词集合（中英文常见停用词） */
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "can", "shall", "to", "of", "in", "for",
            "on", "with", "at", "by", "from", "as", "into", "through", "during",
            "before", "after", "above", "below", "between", "and", "but", "or",
            "not", "no", "so", "if", "then", "than", "too", "very", "just",
            "about", "up", "out", "it", "its", "this", "that", "these", "those",
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都",
            "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会",
            "着", "没有", "看", "好", "自己", "这", "那", "什么", "怎么",
            "如何", "为什么", "哪个", "哪些", "可以", "可能", "应该", "需要",
            "吗", "呢", "吧", "啊", "哦", "嗯"
    );

    /** 最短关键词长度 */
    private static final int MIN_KEYWORD_LENGTH = 2;

    /** 中文字符范围正则 */
    private static final Pattern CJK_PATTERN = Pattern.compile("[\\u4e00-\\u9fff]");

    /**
     * 对检索结果进行基于关键词匹配的重排序。
     */
    @Override
    public List<RetrievalResult> rerank(String queryText, List<RetrievalResult> results) {
        if (results == null || results.isEmpty() || queryText == null || queryText.isBlank()) {
            return results;
        }
        Set<String> queryKeywords = extractKeywords(queryText);
        if (queryKeywords.isEmpty()) {
            log.debug("No valid keywords extracted from query, returning original order");
            return results;
        }
        log.debug("Reranking {} results with {} query keywords: {}", results.size(), queryKeywords.size(), queryKeywords);
        return sortResults(results, queryKeywords);
    }

    /**
     * 对结果排序。
     */
    private List<RetrievalResult> sortResults(List<RetrievalResult> results, Set<String> queryKeywords) {
        List<RetrievalResult> reranked = results.stream()
                .sorted((r1, r2) -> {
                    double score1 = computeFinalScore(r1, queryKeywords);
                    double score2 = computeFinalScore(r2, queryKeywords);
                    return Double.compare(score2, score1);
                })
                .collect(Collectors.toList());
        if (log.isDebugEnabled()) {
            log.debug("Reranking complete. Top result changed: {}", !Objects.equals(results.get(0).chunkId(), reranked.get(0).chunkId()));
        }
        return reranked;
    }

    /**
     * 计算单个结果的融合分数。
     */
    private double computeFinalScore(RetrievalResult result, Set<String> queryKeywords) {
        double keywordScore = computeKeywordScore(result.content(), queryKeywords);
        return ALPHA * result.score() + BETA * keywordScore;
    }

    /**
     * 计算文档内容与查询关键词的匹配分数。
     */
    private double computeKeywordScore(String content, Set<String> queryKeywords) {
        if (content == null || content.isBlank()) {
            return 0.0;
        }
        Set<String> docKeywords = extractKeywords(content);
        if (docKeywords.isEmpty()) {
            return 0.0;
        }
        double coverage = computeCoverage(queryKeywords, docKeywords);
        double normalizedFreq = computeNormalizedFreq(content, queryKeywords);
        return 0.7 * coverage + 0.3 * normalizedFreq;
    }

    /**
     * 计算覆盖率。
     */
    private double computeCoverage(Set<String> queryKeywords, Set<String> docKeywords) {
        long matchedCount = queryKeywords.stream().filter(docKeywords::contains).count();
        return (double) matchedCount / queryKeywords.size();
    }

    /**
     * 计算归一化频次分数。
     */
    private double computeNormalizedFreq(String content, Set<String> queryKeywords) {
        String contentLower = content.toLowerCase();
        double totalFreq = 0.0;
        for (String keyword : queryKeywords) {
            int count = countOccurrences(contentLower, keyword);
            totalFreq += Math.log(1 + count);
        }
        double maxPossibleFreq = queryKeywords.size() * Math.log(1 + 10);
        return Math.min(totalFreq / maxPossibleFreq, 1.0);
    }

    /**
     * 从文本中提取关键词。
     */
    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }
        Set<String> keywords = new HashSet<>();
        String lowerText = text.toLowerCase();
        if (CJK_PATTERN.matcher(lowerText).find()) {
            keywords.addAll(extractChineseKeywords(lowerText));
        }
        addEnglishKeywords(lowerText, keywords);
        return keywords;
    }

    /**
     * 添加英文关键词。
     */
    private void addEnglishKeywords(String lowerText, Set<String> keywords) {
        String[] tokens = lowerText.split("[^a-zA-Z0-9]+");
        for (String token : tokens) {
            token = token.strip();
            if (token.length() >= MIN_KEYWORD_LENGTH && !STOP_WORDS.contains(token)) {
                keywords.add(token);
            }
        }
    }

    /**
     * 提取中文关键词。
     */
    private Set<String> extractChineseKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.CHINESE);
        iterator.setText(text);
        List<String> cjkChars = new ArrayList<>();
        iterateAndExtract(text, iterator, cjkChars, keywords);
        addCJKGrams(cjkChars, keywords);
        return keywords;
    }

    /**
     * 迭代文本并提取中文字符。
     */
    private void iterateAndExtract(String text, BreakIterator iterator,
                                   List<String> cjkChars, Set<String> keywords) {
        int start = iterator.first();
        int end = iterator.next();
        while (end != BreakIterator.DONE) {
            String segment = text.substring(start, end);
            if (CJK_PATTERN.matcher(segment).matches()) {
                cjkChars.add(segment);
            } else {
                addCJKGrams(cjkChars, keywords);
                cjkChars.clear();
            }
            start = end;
            end = iterator.next();
        }
    }

    /**
     * 生成中文字符的 n-gram（2-4 字）作为关键词。
     */
    private void addCJKGrams(List<String> chars, Set<String> keywords) {
        for (int n = 2; n <= 4 && n <= chars.size(); n++) {
            for (int i = 0; i <= chars.size() - n; i++) {
                String gram = buildGram(chars, i, n);
                if (!STOP_WORDS.contains(gram)) {
                    keywords.add(gram);
                }
            }
        }
    }

    /**
     * 构建 n-gram 字符串。
     */
    private String buildGram(List<String> chars, int start, int n) {
        StringBuilder sb = new StringBuilder();
        for (int j = start; j < start + n; j++) {
            sb.append(chars.get(j));
        }
        return sb.toString();
    }

    /**
     * 计算子字符串在文本中出现的次数。
     */
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }
}
