package com.agenthub.application.usecase;

import com.agenthub.application.dto.ChatAttachmentOutput;
import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.domain.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
public class ChatAttachmentUseCase {

    private static final int MAX_CONTEXT_CHARS = 2000000000;
    private final Tika tika = new Tika();
    private final DocumentFileStoragePort documentFileStoragePort;

    public ChatAttachmentUseCase(DocumentFileStoragePort documentFileStoragePort) {
        this.documentFileStoragePort = documentFileStoragePort;
    }

    public List<ChatAttachmentOutput> upload(String sessionId, List<MultipartFile> files) {
        validateFiles(files);
        return files.stream().map(file -> store(sessionId, file)).toList();
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) throw new ValidationException("请选择要上传的文件");
    }

    private ChatAttachmentOutput store(String sessionId, MultipartFile file) {
        try {
            return doStore(sessionId, file);
        } catch (IOException e) {
            throw new ValidationException("文件上传失败: " + file.getOriginalFilename());
        }
    }

    private ChatAttachmentOutput doStore(String sessionId, MultipartFile file) throws IOException {
        String target = resolveUploadPath(sessionId, file.getOriginalFilename());
        String store = documentFileStoragePort.store(target, file.getInputStream(), file.getSize());
        return output(file, store);
    }

    private String resolveUploadPath(String sessionId, String fileName) {
        return String.format("agenthub/session/%s/documents/%s", sessionId, fileName);
    }

    private ChatAttachmentOutput output(MultipartFile file, String path) {
        return new ChatAttachmentOutput(file.getOriginalFilename(), path, file.getContentType(), file.getSize());
    }

    public String readFilesContent(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) return "";
        StringBuilder builder = new StringBuilder("以下是用户上传文件内容，请作为本轮对话上下文：");
        filePaths.forEach(path -> appendFile(builder, path));
        return builder.toString();
    }

    private void appendFile(StringBuilder builder, String filePath) {
        builder.append("\n\n--- 文件: ").append(filePath).append(" ---\n");
        builder.append(readLimited(filePath));
    }

    private String readLimited(String path) {
        try (InputStream input = documentFileStoragePort.retrieve(path)) {
            String content = tika.parseToString(input, new Metadata(), MAX_CONTEXT_CHARS + 1);
            return limitContent(content);
        } catch (IOException | TikaException e) {
            log.warn("Failed to read chat attachment: {}", path, e);
            throw new ValidationException("附件读取失败: " + path);
        }
    }

    private String limitContent(String content) {
        if (content.length() <= MAX_CONTEXT_CHARS) return content;
        return content.substring(0, MAX_CONTEXT_CHARS) + "\n[内容已截断]";
    }
}
