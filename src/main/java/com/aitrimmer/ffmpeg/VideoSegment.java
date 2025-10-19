package com.aitrimmer.ffmpeg;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a trimmed portion of a source video.
 */
public final class VideoSegment {
    private final Path source;
    private final Duration start;
    private final Duration end;

    private VideoSegment(Builder builder) {
        this.source = Objects.requireNonNull(builder.source, "source");
        this.start = builder.start;
        this.end = builder.end;
        if (start != null && start.isNegative()) {
            throw new IllegalArgumentException("start must be non-negative");
        }
        if (end != null && end.isNegative()) {
            throw new IllegalArgumentException("end must be non-negative");
        }
        if (start != null && end != null && end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("end must be greater than start");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Path getSource() {
        return source;
    }

    public Optional<Duration> getStart() {
        return Optional.ofNullable(start);
    }

    public Optional<Duration> getEnd() {
        return Optional.ofNullable(end);
    }

    public Optional<Duration> getDuration() {
        if (start != null && end != null) {
            return Optional.of(end.minus(start));
        }
        return Optional.empty();
    }

    public static final class Builder {
        private Path source;
        private Duration start;
        private Duration end;

        private Builder() {
        }

        public Builder source(Path source) {
            this.source = source;
            return this;
        }

        public Builder start(Duration start) {
            this.start = start;
            return this;
        }

        public Builder end(Duration end) {
            this.end = end;
            return this;
        }

        public VideoSegment build() {
            return new VideoSegment(this);
        }
    }
}
