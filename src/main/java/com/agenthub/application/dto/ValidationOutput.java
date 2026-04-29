package com.agenthub.application.dto;

import java.util.List;

/**
 * @author huangdayu
 */
public record ValidationOutput(boolean valid, List<String> violations) {

}