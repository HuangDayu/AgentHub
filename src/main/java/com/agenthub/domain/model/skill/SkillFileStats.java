package com.agenthub.domain.model.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillFileStats {

    private int fileCount;
    private long totalSize;

}
