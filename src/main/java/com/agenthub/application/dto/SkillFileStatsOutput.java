package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillFileStatsOutput {

    private int fileCount;
    private long totalSize;

}
