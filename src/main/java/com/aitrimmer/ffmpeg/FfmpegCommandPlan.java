package com.aitrimmer.ffmpeg;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Represents a ready-to-run FFmpeg command, optionally backed by a concat script file.
 */
public interface FfmpegCommandPlan {
    /**
     * Produces the full command with all arguments. If the plan uses an auxiliary concat script,
     * the caller must provide the path via {@code scriptPath}.
     */
    List<String> command(Path scriptPath);

    /**
     * Provides the concat script contents when this plan requires one.
     */
    default Optional<String> concatScript() {
        return Optional.empty();
    }
}
