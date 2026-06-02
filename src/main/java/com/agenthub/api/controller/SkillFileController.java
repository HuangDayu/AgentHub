package com.agenthub.api.controller;

import com.agenthub.application.dto.SkillFileOutput;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.usecase.SkillFileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 技能文件控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skills/{skillId}/files")
@RequiredArgsConstructor
public class SkillFileController {

    private final SkillFileUseCase skillFileUseCase;

    /**
     * 获取文件列表。
     */
    @GetMapping
    public ResponseEntity<List<SkillFileOutput>> getFiles(
            @PathVariable String workspaceId,
            @PathVariable String skillId) {
        return ResponseEntity.ok(skillFileUseCase.getSkillFiles(skillId));
    }

    /**
     * 获取文件元数据。
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<SkillFileOutput> getFile(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String fileId) {
        return skillFileUseCase.getFile(skillId, fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取文件文本内容。二进制文件返回提示信息。
     */
    @GetMapping("/{fileId}/content")
    public ResponseEntity<Map<String, String>> getFileContent(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String fileId) {
        String content = skillFileUseCase.getFileContent(skillId, fileId);
        return ResponseEntity.ok(Map.of("content", content));
    }

    /**
     * 删除文件。
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String fileId) {
        skillFileUseCase.deleteFile(skillId, fileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 按扩展名查找文件。
     */
    @GetMapping("/ext/{ext}")
    public ResponseEntity<List<SkillFileOutput>> getFilesByExt(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String ext) {
        return ResponseEntity.ok(skillFileUseCase.getFilesByExt(skillId, ext));
    }

    /**
     * 获取文件统计。
     */
    @GetMapping("/stats")
    public ResponseEntity<SkillFileRepository.FileStats> getStats(
            @PathVariable String workspaceId,
            @PathVariable String skillId) {
        return ResponseEntity.ok(skillFileUseCase.getStats(skillId));
    }
}
