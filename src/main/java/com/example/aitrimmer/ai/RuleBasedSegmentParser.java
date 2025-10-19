package com.example.aitrimmer.ai;

import com.example.aitrimmer.ai.VideoSegmentModels.VideoExtractionRequest;
import com.example.aitrimmer.ai.VideoSegmentModels.VideoSegment;
import com.example.aitrimmer.ai.VideoSegmentModels.VideoSegmentExtractionResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple deterministic parser that extracts segments by scanning the transcript
 * for occurrences of the configured viewer options. Used as a fallback when the
 * LLM response cannot be trusted.
 */
public class RuleBasedSegmentParser {

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})");

    public VideoSegmentExtractionResponse parse(VideoExtractionRequest request) {
        List<VideoSegment> segments = new ArrayList<>();
        String lowerTranscript = request.transcript() == null ? "" : request.transcript().toLowerCase(Locale.ROOT);

        for (String option : request.viewerOptions()) {
            String lowerOption = option.toLowerCase(Locale.ROOT);
            int index = lowerTranscript.indexOf(lowerOption);
            if (index >= 0) {
                double start = findPreviousTimestamp(lowerTranscript, index);
                double end = findNextTimestamp(lowerTranscript, index + lowerOption.length());
                if (Double.compare(end, start) <= 0) {
                    end = start + Math.min(request.maxDurationSeconds(), 30);
                }
                segments.add(new VideoSegment(option, start, Math.min(end, start + request.maxDurationSeconds()),
                        "Contains viewer requested moment: " + option, option));
            }
        }

        segments.sort(Comparator.comparingDouble(VideoSegment::startSeconds));
        return new VideoSegmentExtractionResponse(List.copyOf(segments), request.viewerOptions(), 0.25);
    }

    private double findPreviousTimestamp(String transcript, int position) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(transcript);
        double candidate = 0;
        while (matcher.find()) {
            if (matcher.start() > position) {
                break;
            }
            candidate = toSeconds(matcher.group(1), matcher.group(2));
        }
        return candidate;
    }

    private double findNextTimestamp(String transcript, int position) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(transcript.substring(position));
        if (matcher.find()) {
            return toSeconds(matcher.group(1), matcher.group(2));
        }
        return toSeconds("0", "0");
    }

    private double toSeconds(String minutesPart, String secondsPart) {
        try {
            int minutes = Integer.parseInt(minutesPart);
            int seconds = Integer.parseInt(secondsPart);
            return minutes * 60.0 + seconds;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
