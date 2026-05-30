package com.agenthub.infrastructure.skills;

import com.agenthub.application.port.out.tools.SkillToolScannerPort;
import com.agenthub.domain.model.tools.Skill;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 技能文件管理器，负责技能路径下的文件扫描、网络压缩包的下载解压。
 *
 * @author huangdayu
 */
@Slf4j
@Component
public class SkillFileManager implements SkillToolScannerPort {


    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private final Map<String, Skill> allSkills = new ConcurrentHashMap<>();

    @Override
    public List<Skill> scanSkills(String skillsPath) {
        return scanSkills(Path.of(skillsPath)).values().stream().toList();
    }

    public Map<String, Skill> scanSkills(Path skillsRootPath) {
        log.info("Scanning skills in: {}", skillsRootPath);
        try (Stream<Path> stream = Files.list(skillsRootPath)) {
            Map<String, Skill> skillMap = loadAllSkills(stream);
            allSkills.putAll(skillMap);
            log.info("Scanned {} skills , sum {} skills", skillMap.size(), allSkills.size());
            return skillMap;
        } catch (IOException e) {
            log.error("Failed to scan skills", e);
        }
        return Map.of();
    }

    private Map<String, Skill> loadAllSkills(Stream<Path> stream) {
        Map<String, Skill> cache = new HashMap<>();
        stream.filter(Files::isDirectory).forEach(p -> loadSkillFromPath(p).ifPresent(s -> cache.put(s.getSkillCode(), s)));
        return cache;
    }

    private Optional<Skill> loadSkillFromPath(Path skillPath) {
        Path skillMdPath = skillPath.resolve("SKILL.md");
        if (!Files.exists(skillMdPath)) return Optional.empty();
        try {
            return parseSkillMd(skillPath, skillMdPath);
        } catch (IOException e) {
            log.error("Failed to read SKILL.md in: {}", skillPath, e);
            return Optional.empty();
        }
    }

    private Optional<Skill> parseSkillMd(Path skillPath, Path skillMdPath) throws IOException {
        List<String> lines = Files.readAllLines(skillMdPath);
        if (lines.size() < 4) return Optional.empty();
        String skillCode = skillPath.getFileName().toString();
        String name = lines.get(1).trim().replace("name:", "");
        String description = lines.get(2).trim().replace("description:", "");
        String path = skillPath.toString();
        String skillFilesTree = buildFilesTreeJson(skillPath);
        return Optional.of(createSkill(skillCode, name, description, path, skillFilesTree));
    }

    private Skill createSkill(String skillCode, String name, String description, String path, String skillFilesTree) {
        Skill skill = new Skill();
        skill.setSkillCode(skillCode);
        skill.setName(name);
        skill.setDescription(description);
        skill.setSkillType("FILE");
        skill.setSkillPath(path);
        skill.setSkillFilesTree(skillFilesTree);
        skill.setEnabled(true);
        skill.setCreatedAt(Instant.now());
        skill.setUpdatedAt(Instant.now());
        return skill;
    }

    private String buildFilesTreeJson(Path skillPath) {
        try {
            Map<String, Object> tree = buildDirectoryTree(skillPath, skillPath);
            return objectMapper.writeValueAsString(tree);
        } catch (Exception e) {
            log.error("Failed to build files tree for: {}", skillPath, e);
            return "{}";
        }
    }

    private Map<String, Object> buildDirectoryTree(Path rootPath, Path currentPath) throws IOException {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("name", currentPath.getFileName().toString());
        node.put("path", rootPath.relativize(currentPath).toString().replace("\\", "/"));
        BasicFileAttributes attrs = Files.readAttributes(currentPath, BasicFileAttributes.class);
        node.put("isDirectory", attrs.isDirectory());
        node.put("size", attrs.size());
        node.put("lastModified", attrs.lastModifiedTime().toInstant().toString());
        if (attrs.isDirectory()) addChildren(rootPath, currentPath, node);
        return node;
    }

    private void addChildren(Path rootPath, Path currentPath, Map<String, Object> node) throws IOException {
        List<Map<String, Object>> children = new ArrayList<>();
        try (Stream<Path> stream = Files.list(currentPath)) {
            stream.sorted(Comparator.comparing(Path::getFileName))
                    .forEach(p -> addChild(rootPath, p, children));
        }
        node.put("children", children);
    }

    private void addChild(Path rootPath, Path p, List<Map<String, Object>> children) {
        try {
            children.add(buildDirectoryTree(rootPath, p));
        } catch (IOException e) {
            log.warn("Failed to process: {}", p, e);
        }
    }

    public Skill downloadAndExtract(Path skillsRootPath, String url, String skillCode) throws IOException {
        log.info("Downloading skill from: {}", url);
        Path targetPath = skillsRootPath.resolve(skillCode);
        if (Files.exists(targetPath)) deleteDirectory(targetPath);
        Files.createDirectories(targetPath);
        extractZip(url, targetPath);
        log.info("Extracted skill to: {}", targetPath);
        return loadExtractedSkill(skillCode, targetPath);
    }

    private void extractZip(String url, Path targetPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new URL(url).openStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                extractEntry(targetPath, zis, entry);
                zis.closeEntry();
            }
        }
    }

    private void extractEntry(Path targetPath, ZipInputStream zis, ZipEntry entry) throws IOException {
        Path entryPath = targetPath.resolve(entry.getName());
        if (entry.isDirectory()) {
            Files.createDirectories(entryPath);
        } else {
            Files.createDirectories(entryPath.getParent());
            Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Skill loadExtractedSkill(String skillCode, Path targetPath) throws IOException {
        Optional<Skill> skill = loadSkillFromPath(targetPath);
        if (skill.isPresent()) {
            return skill.get();
        }
        throw new IOException("Failed to load skill after extraction: " + skillCode);
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(this::deletePath);
        }
    }

    private void deletePath(Path p) {
        try {
            Files.delete(p);
        } catch (IOException e) {
            log.warn("Failed to delete: {}", p, e);
        }
    }

    public Path getSkillPath(Path skillsRootPath, String skillCode) {
        return skillsRootPath.resolve(skillCode);
    }


    public void refresh(Path skillsRootPath) {
        scanSkills(skillsRootPath);
        log.info("Skill cache refreshed");
    }

    public void deleteSkill(Path skillsRootPath, String skillCode) throws IOException {
        Path skillPath = skillsRootPath.resolve(skillCode);
        deleteDirectory(skillPath);
        log.info("Deleted skill: {}", skillCode);
    }


}
