package com.agenthub.domain.model;

import java.util.Objects;

import static com.agenthub.common.utils.RandomUtils.randomId;

/** 工具唯一标识值对象。 */
public record HttpToolId(
        /** 标识值 */
        String value) {
    public HttpToolId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tool id must not be blank");
        }
    }

    public static HttpToolId newId() {
        return new HttpToolId(randomId());
    }

    public static HttpToolId of(String value) {
        return new HttpToolId(value);
    }

    @Override
    public String value() {
        return Objects.requireNonNull(value);
    }
}
