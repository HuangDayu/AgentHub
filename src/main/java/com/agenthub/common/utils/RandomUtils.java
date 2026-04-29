package com.agenthub.common.utils;

import java.util.UUID;

/**
 * @author huangdayu
 */
public class RandomUtils {

    public static String randomId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String randomShortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
