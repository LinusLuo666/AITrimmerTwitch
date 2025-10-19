package com.example.aitrimmer.service;

import com.example.aitrimmer.ai.PromptTemplates;
import com.example.aitrimmer.ai.RuleBasedSegmentParser;
import com.example.aitrimmer.ai.VideoSegmentModels.VideoExtractionRequest;
import com.example.aitrimmer.ai.VideoSegmentModels.VideoSegmentExtractionResponse;
import com.example.aitrimmer.logging.PromptAuditLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Orchestrates the call to the Spring AI {@link ChatClient} and ensures that
 * responses comply with the expected schema. When the model fails validation or
 * reports a low confidence value, a deterministic rule-based strategy is used
 * instead.
 */
@Service
public class VideoSegmentExtractionService {

    private final ChatClient chatClient;
    private final PromptAuditLogger auditLogger;
    private final ObjectMapper objectMapper;
    private final RuleBasedSegmentParser fallbackParser;
    private final double confidenceThreshold;

    public VideoSegmentExtractionService(ChatClient chatClient,
                                         PromptAuditLogger auditLogger,
                                         ObjectMapper objectMapper) {
        this(chatClient, auditLogger, objectMapper, new RuleBasedSegmentParser(), 0.6);
    }

    public VideoSegmentExtractionService(ChatClient chatClient,
                                         PromptAuditLogger auditLogger,
                                         ObjectMapper objectMapper,
                                         RuleBasedSegmentParser fallbackParser,
                                         double confidenceThreshold) {
        this.chatClient = chatClient;
        this.auditLogger = auditLogger;
        this.objectMapper = objectMapper;
        this.fallbackParser = fallbackParser;
        this.confidenceThreshold = confidenceThreshold;
    }

    public VideoSegmentExtractionResponse extractSegments(VideoExtractionRequest request) {
        PromptTemplate template = new PromptTemplate(PromptTemplates.SEGMENT_EXTRACTION_PROMPT);
        Map<String, Object> variables = Map.of(
                "transcript", request.transcript(),
                "options", request.viewerOptions(),
                "maxDuration", request.maxDurationSeconds());
        String promptText = template.render(variables);
        auditLogger.logPrompt(promptText);

        ChatClient.CallResponse response = chatClient.prompt()
                .user(promptText)
                .withStructuredOutput(PromptTemplates.SEGMENT_OUTPUT_SCHEMA)
                .call();

        String content = response.content();
        auditLogger.logResponse(content);

        VideoSegmentExtractionResponse parsed = deserialize(content);
        if (!isValid(parsed)) {
            return fallbackParser.parse(request);
        }
        if (parsed.confidence() < confidenceThreshold) {
            return fallbackParser.parse(request);
        }
        return parsed;
    }

    private VideoSegmentExtractionResponse deserialize(String content) {
        if (content == null || content.isBlank()) {
            return new VideoSegmentExtractionResponse(java.util.List.of(), java.util.List.of(), 0);
        }
        try {
            return objectMapper.readValue(content, VideoSegmentExtractionResponse.class);
        } catch (JsonProcessingException ex) {
            return new VideoSegmentExtractionResponse(java.util.List.of(), java.util.List.of(), 0);
        }
    }

    private boolean isValid(VideoSegmentExtractionResponse response) {
        if (response == null) {
            return false;
        }
        if (response.confidence() < 0 || response.confidence() > 1) {
            return false;
        }
        return response.segments().stream().allMatch(segment ->
                segment.title() != null && !segment.title().isBlank() &&
                        segment.endSeconds() > segment.startSeconds());
    }
}
