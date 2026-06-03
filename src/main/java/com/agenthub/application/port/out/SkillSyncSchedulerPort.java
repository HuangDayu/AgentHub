package com.agenthub.application.port.out;

import com.agenthub.domain.model.skill.SkillConfig;

/**
 * 技能同步调度器出端口。
 */
public interface SkillSyncSchedulerPort {

    /**
     * 注册定时任务。
     */
    void schedule(SkillConfig config);

    /**
     * 取消定时任务。
     */
    void unschedule(String configId);

    /**
     * 重新调度定时任务。
     */
    void reschedule(SkillConfig config);
}
