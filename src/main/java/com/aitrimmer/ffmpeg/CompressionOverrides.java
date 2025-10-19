package com.aitrimmer.ffmpeg;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents user-defined overrides for a compression preset.
 */
public final class CompressionOverrides {
    private final String videoCodec;
    private final String audioCodec;
    private final String videoBitrate;
    private final String audioBitrate;
    private final Integer crf;
    private final String preset;
    private final List<String> extraArgs;

    private CompressionOverrides(Builder builder) {
        this.videoCodec = builder.videoCodec;
        this.audioCodec = builder.audioCodec;
        this.videoBitrate = builder.videoBitrate;
        this.audioBitrate = builder.audioBitrate;
        this.crf = builder.crf;
        this.preset = builder.preset;
        this.extraArgs = builder.extraArgs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CompressionOverrides fromMap(Map<String, String> values) {
        Objects.requireNonNull(values, "values");
        Builder builder = builder();
        if (values.containsKey("videoCodec")) {
            builder.videoCodec(values.get("videoCodec"));
        }
        if (values.containsKey("audioCodec")) {
            builder.audioCodec(values.get("audioCodec"));
        }
        if (values.containsKey("videoBitrate")) {
            builder.videoBitrate(values.get("videoBitrate"));
        }
        if (values.containsKey("audioBitrate")) {
            builder.audioBitrate(values.get("audioBitrate"));
        }
        if (values.containsKey("crf")) {
            builder.crf(Integer.parseInt(values.get("crf")));
        }
        if (values.containsKey("preset")) {
            builder.preset(values.get("preset"));
        }
        if (values.containsKey("extraArgs")) {
            builder.extraArgs(List.of(values.get("extraArgs").split(",")));
        }
        return builder.build();
    }

    public Optional<String> getVideoCodec() {
        return Optional.ofNullable(videoCodec);
    }

    public Optional<String> getAudioCodec() {
        return Optional.ofNullable(audioCodec);
    }

    public Optional<String> getVideoBitrate() {
        return Optional.ofNullable(videoBitrate);
    }

    public Optional<String> getAudioBitrate() {
        return Optional.ofNullable(audioBitrate);
    }

    public Optional<Integer> getCrf() {
        return Optional.ofNullable(crf);
    }

    public Optional<String> getPreset() {
        return Optional.ofNullable(preset);
    }

    public Optional<List<String>> getExtraArgs() {
        return Optional.ofNullable(extraArgs);
    }

    public static final class Builder {
        private String videoCodec;
        private String audioCodec;
        private String videoBitrate;
        private String audioBitrate;
        private Integer crf;
        private String preset;
        private List<String> extraArgs;

        private Builder() {
        }

        public Builder videoCodec(String videoCodec) {
            this.videoCodec = videoCodec;
            return this;
        }

        public Builder audioCodec(String audioCodec) {
            this.audioCodec = audioCodec;
            return this;
        }

        public Builder videoBitrate(String videoBitrate) {
            this.videoBitrate = videoBitrate;
            return this;
        }

        public Builder audioBitrate(String audioBitrate) {
            this.audioBitrate = audioBitrate;
            return this;
        }

        public Builder crf(Integer crf) {
            this.crf = crf;
            return this;
        }

        public Builder preset(String preset) {
            this.preset = preset;
            return this;
        }

        public Builder extraArgs(List<String> extraArgs) {
            this.extraArgs = extraArgs == null ? null : List.copyOf(extraArgs);
            return this;
        }

        public CompressionOverrides build() {
            return new CompressionOverrides(this);
        }
    }
}
