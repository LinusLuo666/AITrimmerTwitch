package com.aitrimmer.ffmpeg;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Builds FFmpeg command invocations from a list of segments.
 */
public final class FfmpegCommandBuilder {

    public enum Strategy {
        CONCAT_SCRIPT,
        FILTER_COMPLEX
    }

    private final CompressionPresetConfig presetConfig;

    public FfmpegCommandBuilder(CompressionPresetConfig presetConfig) {
        this.presetConfig = Objects.requireNonNull(presetConfig, "presetConfig");
    }

    public FfmpegCommandPlan build(
            List<VideoSegment> segments,
            Path output,
            CompressionOverrides overrides,
            Strategy strategy
    ) {
        Objects.requireNonNull(segments, "segments");
        Objects.requireNonNull(output, "output");
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("At least one segment is required");
        }
        CompressionPreset preset = presetConfig.resolve(overrides);
        return switch (strategy) {
            case CONCAT_SCRIPT -> buildConcatPlan(segments, output, preset);
            case FILTER_COMPLEX -> buildFilterPlan(segments, output, preset);
        };
    }

    private FfmpegCommandPlan buildConcatPlan(List<VideoSegment> segments, Path output, CompressionPreset preset) {
        String script = buildConcatScript(segments);
        List<String> prefix = new ArrayList<>();
        prefix.add("ffmpeg");
        prefix.add("-y");
        prefix.add("-hide_banner");
        prefix.add("-safe");
        prefix.add("0");
        prefix.add("-f");
        prefix.add("concat");
        prefix.add("-i");

        List<String> suffix = new ArrayList<>(preset.toArgs());
        suffix.add(output.toString());
        return new ConcatDemuxerPlan(script, prefix, suffix);
    }

    private String buildConcatScript(List<VideoSegment> segments) {
        String lineSeparator = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        for (VideoSegment segment : segments) {
            builder.append("file '")
                    .append(escapePath(segment.getSource()))
                    .append("'")
                    .append(lineSeparator);
            segment.getStart().ifPresent(start ->
                    builder.append("inpoint ")
                            .append(formatDuration(start))
                            .append(lineSeparator)
            );
            segment.getEnd().ifPresent(end ->
                    builder.append("outpoint ")
                            .append(formatDuration(end))
                            .append(lineSeparator)
            );
        }
        return builder.toString();
    }

    private FfmpegCommandPlan buildFilterPlan(List<VideoSegment> segments, Path output, CompressionPreset preset) {
        List<String> args = new ArrayList<>();
        args.add("ffmpeg");
        args.add("-y");
        args.add("-hide_banner");
        for (VideoSegment segment : segments) {
            args.add("-i");
            args.add(segment.getSource().toString());
        }
        String filterGraph = buildFilterGraph(segments);
        args.add("-filter_complex");
        args.add(filterGraph);
        args.add("-map");
        args.add("[vout]");
        args.add("-map");
        args.add("[aout]");
        args.addAll(preset.toArgs());
        args.add(output.toString());
        return new FilterGraphPlan(args);
    }

    private String buildFilterGraph(List<VideoSegment> segments) {
        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            VideoSegment segment = segments.get(i);
            String videoLabel = "v" + i;
            String audioLabel = "a" + i;
            filter.append("[")
                    .append(i)
                    .append(":v]");
            String videoTrim = buildTrimParameters(segment.getStart(), segment.getEnd());
            if (!videoTrim.isEmpty()) {
                filter.append("trim=")
                        .append(videoTrim)
                        .append(",");
            }
            filter.append("setpts=PTS-STARTPTS[")
                    .append(videoLabel)
                    .append("];");

            filter.append("[")
                    .append(i)
                    .append(":a]");
            String audioTrim = buildTrimParameters(segment.getStart(), segment.getEnd());
            if (!audioTrim.isEmpty()) {
                filter.append("atrim=")
                        .append(audioTrim)
                        .append(",");
            }
            filter.append("asetpts=PTS-STARTPTS[")
                    .append(audioLabel)
                    .append("];");
        }
        for (int i = 0; i < segments.size(); i++) {
            filter.append("[v")
                    .append(i)
                    .append("][a")
                    .append(i)
                    .append("]");
        }
        filter.append("concat=n=")
                .append(segments.size())
                .append(":v=1:a=1[vout][aout]");
        return filter.toString();
    }

    private String buildTrimParameters(java.util.Optional<Duration> startOpt, java.util.Optional<Duration> endOpt) {
        List<String> params = new ArrayList<>();
        startOpt.filter(duration -> !duration.isZero())
                .ifPresent(duration -> params.add("start=" + formatDuration(duration)));
        endOpt.ifPresent(duration -> params.add("end=" + formatDuration(duration)));
        StringJoiner joiner = new StringJoiner(":");
        for (String param : params) {
            joiner.add(param);
        }
        return joiner.toString();
    }

    private String escapePath(Path path) {
        String text = path.toString();
        return text.replace("'", "'\\''");
    }

    private String formatDuration(Duration duration) {
        BigDecimal seconds = BigDecimal.valueOf(duration.toNanos())
                .divide(BigDecimal.valueOf(1_000_000_000L), 6, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        String formatted = seconds.toPlainString();
        if (formatted.indexOf('.') < 0) {
            formatted = formatted + ".0";
        }
        return formatted;
    }
}
