package com.aitrimmer.ffmpeg;

/**
 * Wraps failures encountered while invoking FFmpeg.
 */
public class FfmpegExecutionException extends RuntimeException {
    public FfmpegExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FfmpegExecutionException(String message) {
        super(message);
    }
}
