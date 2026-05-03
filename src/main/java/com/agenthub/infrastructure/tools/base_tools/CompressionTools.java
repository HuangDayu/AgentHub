package com.agenthub.infrastructure.tools.base_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

@AgentTools(defaultEnable = false)
public class CompressionTools {

    @Tool(name = "compress_gzip_string", description = "GZIP compress string to base64")
    public String gzipCompressString(String text) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return java.util.Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    @Tool(name = "decompress_gzip_string", description = "GZIP decompress from base64")
    public String gzipDecompressString(String compressed) throws Exception {
        byte[] bytes = java.util.Base64.getDecoder().decode(compressed);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    @Tool(name = "compress_deflate_string", description = "Deflate compress string to base64")
    public String deflateCompressString(String text) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
            dos.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return java.util.Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    @Tool(name = "decompress_deflate_string", description = "Deflate decompress from base64")
    public String deflateDecompressString(String compressed) throws Exception {
        byte[] bytes = java.util.Base64.getDecoder().decode(compressed);
        try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(bytes));
             BufferedReader reader = new BufferedReader(new InputStreamReader(iis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    @Tool(name = "compress_gzip_file", description = "GZIP compress file")
    public String gzipCompressFile(String sourcePath, String targetPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(sourcePath);
             FileOutputStream fos = new FileOutputStream(targetPath);
             GZIPOutputStream gzip = new GZIPOutputStream(fos)) {
            fis.transferTo(gzip);
        }
        return "Compressed to: " + targetPath;
    }

    @Tool(name = "decompress_gzip_file", description = "GZIP decompress file")
    public String gzipDecompressFile(String sourcePath, String targetPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(sourcePath);
             GZIPInputStream gzip = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(targetPath)) {
            gzip.transferTo(fos);
        }
        return "Decompressed to: " + targetPath;
    }

    @Tool(name = "compress_zip_file", description = "Add file to ZIP archive")
    public String zipFile(String sourcePath, String zipPath) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            File file = new File(sourcePath);
            zos.putNextEntry(new ZipEntry(file.getName()));
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.transferTo(zos);
            }
            zos.closeEntry();
        }
        return "Created: " + zipPath;
    }

    @Tool(name = "compress_unzip_file", description = "Extract ZIP archive")
    public String unzipFile(String zipPath, String targetDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(targetDir, entry.getName());
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    zis.transferTo(fos);
                }
                zis.closeEntry();
            }
        }
        return "Extracted to: " + targetDir;
    }

    @Tool(name = "compress_list_zip", description = "List contents of ZIP file")
    public String listZip(String zipPath) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.entries().asIterator()
                .forEachRemaining(e -> sb.append(e.getName()).append(" (").append(e.getSize()).append(" bytes)\n"));
        }
        return sb.toString();
    }

    @Tool(name = "compress_get_ratio", description = "Calculate compression ratio")
    public String getCompressionRatio(String original, String compressed) {
        long origSize = original.length();
        long compSize = compressed.length();
        double ratio = (1 - (double) compSize / origSize) * 100;
        return String.format("Original: %d, Compressed: %d, Ratio: %.2f%%", origSize, compSize, ratio);
    }
}
