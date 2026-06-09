package com.agenthub.infrastructure.tools.data_source.params;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka 消息选项 DTO
 */
@Data
@NoArgsConstructor
public class KafkaOptions {

    /** Topic名称（为空则使用默认topic） */
    private String topic;

    /** 消息key（可选） */
    private String key;

    /** 消息头JSON字符串 */
    private String headers;
}
