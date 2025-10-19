package com.example.aitrimmer.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class VideoSegmentModels {

    private VideoSegmentModels() {
    }

    public record VideoSegment(
            String title,
            double startSeconds,
            double endSeconds,
            String rationale,
            String viewerOption
    ) {
        @JsonCreator
        public VideoSegment(@JsonProperty("title") String title,
                             @JsonProperty("startSeconds") double startSeconds,
                             @JsonProperty("endSeconds") double endSeconds,
                             @JsonProperty("rationale") String rationale,
                             @JsonProperty("viewerOption") String viewerOption) {
            this.title = title;
            this.startSeconds = startSeconds;
            this.endSeconds = endSeconds;
            this.rationale = rationale;
            this.viewerOption = viewerOption;
        }
    }

    public record VideoSegmentExtractionResponse(
            List<VideoSegment> segments,
            List<String> optionSummaries,
            double confidence
    ) {
        @JsonCreator
        public VideoSegmentExtractionResponse(@JsonProperty("segments") List<VideoSegment> segments,
                                              @JsonProperty("optionSummaries") List<String> optionSummaries,
                                              @JsonProperty("confidence") double confidence) {
            this.segments = segments == null ? List.of() : List.copyOf(segments);
            this.optionSummaries = optionSummaries == null ? List.of() : List.copyOf(optionSummaries);
            this.confidence = confidence;
        }
    }

    public record VideoExtractionRequest(
            String transcript,
            List<String> viewerOptions,
            double maxDurationSeconds
    ) {
        @JsonCreator
        public VideoExtractionRequest(@JsonProperty("transcript") String transcript,
                                      @JsonProperty("viewerOptions") List<String> viewerOptions,
                                      @JsonProperty("maxDurationSeconds") double maxDurationSeconds) {
            this.transcript = transcript;
            this.viewerOptions = viewerOptions == null ? List.of() : List.copyOf(viewerOptions);
            this.maxDurationSeconds = maxDurationSeconds;
        }
    }
}
