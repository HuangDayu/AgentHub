package com.agenthub.domain.model;

import java.util.Objects;

import static com.agenthub.common.utils.RandomUtils.randomId;

/** 工具唯一标识值对象。 */
public record ToolId(
        /** 标识值 */
        String value) {
    public ToolId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tool id must not be blank");
        }
    }

    public static ToolId newId() {
        return new ToolId(randomId());
    }

    public static ToolId of(String value) {
        return new ToolId(value);
    }

    @Override
    public String value() {
        return Objects.requireNonNull(value);
    }
}
