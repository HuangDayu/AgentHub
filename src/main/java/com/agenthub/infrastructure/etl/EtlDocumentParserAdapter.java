package com.agenthub.infrastructure.etl;

import com.agenthub.application.port.out.etl.EtlDocumentParserPort;
import com.agenthub.domain.model.DocumentContent;
import com.agenthub.domain.model.DocumentContent.DocumentFormat;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 文档解析适配器，支持多种文档格式的文本提取。
 * <p>
 * 支持以下格式：
 * <ul>
 *   <li>PDF - 使用 Apache Tika 解析</li>
 *   <li>Word (.doc/.docx) - 使用 Apache Tika 解析</li>
 *   <li>TXT / Markdown - 直接读取纯文本</li>
 *   <li>HTML - 使用 Jsoup 提取正文文本</li>
 * </ul>
 * 对于未知格式，会尝试使用 Tika 的自动检测解析器。
 * </p>
 */
@Component
public class EtlDocumentParserAdapter implements EtlDocumentParserPort {

    private static final Logger log = LoggerFactory.getLogger(EtlDocumentParserAdapter.class);

    @Override
    public DocumentContent parse(String documentId, InputStream content,
                                 String contentType, String fileName) {
        DocumentFormat format = DocumentContent.detectFormat(contentType, fileName);
        try {
            String text = switch (format) {
                case HTML -> parseHtml(content);
                case TEXT, MARKDOWN -> parsePlainText(content);
                case JSON,PDF, WORD, EXCEL, UNKNOWN -> parseWithTika(content);
            };
            return DocumentContent.create(documentId, text, format);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse document: " + documentId, e);
        }
    }

    /**
     * 使用 Jsoup 解析 HTML 文档，提取正文纯文本。
     *
     * @param content HTML 输入流
     * @return 提取的纯文本内容
     * @throws IOException 读取流失败时抛出
     */
    private String parseHtml(InputStream content) throws IOException {
        String html = new String(content.readAllBytes(), StandardCharsets.UTF_8);
        return Jsoup.parse(html).body().text();
    }

    /**
     * 直接读取纯文本内容（TXT、Markdown）。
     *
     * @param content 文本输入流
     * @return 读取的文本内容
     * @throws IOException 读取流失败时抛出
     */
    private String parsePlainText(InputStream content) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(content, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 使用 Apache Tika 自动检测并解析文档内容。
     * <p>
     * 适用于 PDF、Word 等二进制文档格式。
     * </p>
     *
     * @param content 文档输入流
     * @return 提取的纯文本内容
     * @throws IOException 解析失败时抛出
     */
    private String parseWithTika(InputStream content) throws IOException {
        try {
            Tika tika = new Tika();
            try (InputStream stream = TikaInputStream.get(content)) {
                return tika.parseToString(stream);
            }
        } catch (Exception e) {
            throw new IOException("Tika parsing failed", e);
        }
    }
}
