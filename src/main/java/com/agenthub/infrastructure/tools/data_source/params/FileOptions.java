package com.agenthub.infrastructure.tools.data_source.params;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件操作选项 DTO
 */
@Data
@NoArgsConstructor
public class FileOptions {

    /** 文件内容（write操作时使用） */
    private String content;
}
