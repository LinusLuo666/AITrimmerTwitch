package com.example.aitrimmer.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Handles audit logging of prompts and responses while ensuring sensitive
 * details such as API keys are redacted.
 */
@Component
public class PromptAuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(PromptAuditLogger.class);

    private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "(?i)(api[_\-]?key\\s*[:=]\\s*)([A-Za-z0-9_\-]{8,})");

    public void logPrompt(String prompt) {
        logger.info("AI prompt: {}", mask(prompt));
    }

    public void logResponse(String response) {
        logger.info("AI response: {}", mask(response));
    }

    private String mask(String value) {
        if (value == null) {
            return null;
        }
        return API_KEY_PATTERN.matcher(value).replaceAll(matchResult -> matchResult.group(1) + "***");
    }
}
