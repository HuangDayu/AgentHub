package com.agenthub.infrastructure.adapter;

import com.agenthub.application.port.out.rag.DocumentStoragePort;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 基于 MinIO 的文档存储适配器。
 *
 * <p>该类实现了 {@link DocumentStoragePort} 接口，使用 MinIO 作为后端存储。
 * 在应用启动时自动检测并创建所需的存储桶（Bucket）。</p>
 *
 * <p>配置示例（application.yml）：</p>
 * <pre>
 * agenthub:
 *   storage:
 *     minio:
 *       endpoint: http://localhost:9000
 *       access-key: minioadmin
 *       secret-key: minioadmin
 *       bucket: agenthub
 * </pre>
 *
 * <p>文件在 MinIO 中的路径格式为：{@code documents/{kbCode}/{timestamp}_{filename}}</p>
 *
 * @author agenthub
 * @see DocumentStoragePort
 */
@Component
public class DocumentStorageAdapter implements DocumentStoragePort {

    private static final Logger log = LoggerFactory.getLogger(DocumentStorageAdapter.class);

    private final MinioClient minioClient;
    private final String bucket;

    /**
     * 构造函数，通过配置属性初始化 MinIO 客户端。
     *
     * @param endpoint  MinIO 服务端点地址
     * @param accessKey MinIO 访问密钥
     * @param secretKey MinIO 秘密密钥
     * @param bucket    MinIO 存储桶名称
     */
    public DocumentStorageAdapter(
            @Value("${agenthub.storage.minio.endpoint}") String endpoint,
            @Value("${agenthub.storage.minio.access-key}") String accessKey,
            @Value("${agenthub.storage.minio.secret-key}") String secretKey,
            @Value("${agenthub.storage.minio.bucket}") String bucket) {
        this.bucket = bucket;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 应用启动后自动检查并创建存储桶（如果不存在）。
     *
     * @throws Exception 如果 MinIO 连接或桶创建失败
     */
    @PostConstruct
    public void init() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build());
            log.info("已创建 MinIO 存储桶: {}", bucket);
        } else {
            log.info("MinIO 存储桶已存在: {}", bucket);
        }
    }

    /**
     * 将文档内容上传到 MinIO。
     *
     * <p>上传后文件在桶中的路径为传入的 path 参数，
     * 建议使用格式 {@code documents/{kbCode}/{timestamp}_{filename}}。</p>
     *
     * @param path    存储路径（对象键）
     * @param content 文档输入流
     * @param size    文档大小（字节数）
     * @return 实际存储路径（与传入的 path 相同）
     */
    @Override
    public String store(String path, InputStream content, long size) {
        try {
            byte[] data = content.readAllBytes();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(new ByteArrayInputStream(data), data.length, -1)
                            .build());
            log.debug("文件已上传到 MinIO: {}/{}", bucket, path);
            return path;
        } catch (Exception e) {
            throw new RuntimeException("上传文件到 MinIO 失败: " + path, e);
        }
    }

    /**
     * 从 MinIO 下载指定路径的文档内容。
     *
     * @param path 存储路径（对象键）
     * @return 文档输入流；如果文件不存在则返回空流
     */
    @Override
    public InputStream retrieve(String path) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .build())) {
            log.debug("文件已从 MinIO 下载: {}/{}", bucket, path);
            return new ByteArrayInputStream(stream.readAllBytes());
        } catch (Exception e) {
            log.warn("从 MinIO 获取文件失败: {}/{}", bucket, path, e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.deleteObjectTags(DeleteObjectTagsArgs.builder().bucket(bucket).object(path).build());
        } catch (Exception e) {
            throw new RuntimeException("删除MinIO文件失败: " + path, e);
        }
    }
}
