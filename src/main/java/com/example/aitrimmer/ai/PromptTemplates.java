package com.example.aitrimmer.ai;

import java.util.Map;

/**
 * Central place for the natural language prompts that are shared across the
 * application. Each prompt contains handlebars style placeholders which can be
 * resolved by the {@code PromptTemplate} support provided by Spring AI.
 */
public final class PromptTemplates {

    private PromptTemplates() {
    }

    public static final String SEGMENT_EXTRACTION_PROMPT = String.join("\n",
            "You are an assistant that extracts highlight-worthy segments from Twitch streams.",
            "Use the provided transcript, viewer options, and duration to propose concise clip metadata.",
            "Return JSON that strictly matches the output schema.",
            "Transcript: {{transcript}}",
            "Available viewer options: {{options}}",
            "Maximum clip duration (seconds): {{maxDuration}}",
            "Respond with segments that include title, start and end offsets, rationale, and the matching viewer option.");

    public static final String OPTION_SUMMARY_PROMPT = String.join("\n",
            "You summarise available highlight options for a streamer.",
            "Create short display labels for each option focusing on the mood and key moments.",
            "Options: {{options}}",
            "Return a JSON array of strings named 'optionSummaries'.");

    /**
     * Output schema that the model must follow. Exposed as a JSON schema string so it can
     * be referenced by Spring AI's structured output features.
     */
    public static final Map<String, Object> SEGMENT_OUTPUT_SCHEMA = Map.of(
            "$schema", "https://json-schema.org/draft/2020-12/schema",
            "title", "VideoSegmentExtraction",
            "type", "object",
            "required", java.util.List.of("segments", "confidence", "optionSummaries"),
            "properties", Map.of(
                    "segments", Map.of(
                            "type", "array",
                            "items", Map.of(
                                    "type", "object",
                                    "required", java.util.List.of("title", "startSeconds", "endSeconds", "rationale", "viewerOption"),
                                    "properties", Map.of(
                                            "title", Map.of("type", "string"),
                                            "startSeconds", Map.of("type", "number", "minimum", 0),
                                            "endSeconds", Map.of("type", "number", "minimum", 0),
                                            "rationale", Map.of("type", "string"),
                                            "viewerOption", Map.of("type", "string")
                                    )
                            )
                    ),
                    "confidence", Map.of("type", "number", "minimum", 0, "maximum", 1),
                    "optionSummaries", Map.of(
                            "type", "array",
                            "items", Map.of("type", "string")
                    )
            )
    );
}
