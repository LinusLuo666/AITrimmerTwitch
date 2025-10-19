package com.aitrimmer.ffmpeg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a reusable collection of FFmpeg compression arguments.
 */
public final class CompressionPreset {
    private final String videoCodec;
    private final String audioCodec;
    private final String videoBitrate;
    private final String audioBitrate;
    private final Integer crf;
    private final String preset;
    private final List<String> extraArgs;

    public CompressionPreset(
            String videoCodec,
            String audioCodec,
            String videoBitrate,
            String audioBitrate,
            Integer crf,
            String preset,
            List<String> extraArgs
    ) {
        this.videoCodec = Objects.requireNonNullElse(videoCodec, "libx264");
        this.audioCodec = Objects.requireNonNullElse(audioCodec, "aac");
        this.videoBitrate = videoBitrate;
        this.audioBitrate = audioBitrate;
        this.crf = crf;
        this.preset = Objects.requireNonNullElse(preset, "medium");
        this.extraArgs = List.copyOf(extraArgs == null ? List.of() : extraArgs);
    }

    public static CompressionPreset createDefault() {
        return new CompressionPreset("libx264", "aac", null, "128k", 23, "medium", List.of());
    }

    public CompressionPreset withOverrides(CompressionOverrides overrides) {
        if (overrides == null) {
            return this;
        }
        return new CompressionPreset(
                overrides.getVideoCodec().orElse(videoCodec),
                overrides.getAudioCodec().orElse(audioCodec),
                overrides.getVideoBitrate().orElse(videoBitrate),
                overrides.getAudioBitrate().orElse(audioBitrate),
                overrides.getCrf().orElse(crf),
                overrides.getPreset().orElse(preset),
                overrides.getExtraArgs().orElse(extraArgs)
        );
    }

    public List<String> toArgs() {
        List<String> args = new ArrayList<>();
        args.add("-c:v");
        args.add(videoCodec);
        if (videoBitrate != null && !videoBitrate.isBlank()) {
            args.add("-b:v");
            args.add(videoBitrate);
        }
        if (preset != null && !preset.isBlank()) {
            args.add("-preset");
            args.add(preset);
        }
        if (crf != null) {
            args.add("-crf");
            args.add(Integer.toString(crf));
        }
        args.add("-c:a");
        args.add(audioCodec);
        if (audioBitrate != null && !audioBitrate.isBlank()) {
            args.add("-b:a");
            args.add(audioBitrate);
        }
        args.addAll(extraArgs);
        return Collections.unmodifiableList(args);
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public String getVideoBitrate() {
        return videoBitrate;
    }

    public String getAudioBitrate() {
        return audioBitrate;
    }

    public Integer getCrf() {
        return crf;
    }

    public String getPreset() {
        return preset;
    }

    public List<String> getExtraArgs() {
        return extraArgs;
    }
}
