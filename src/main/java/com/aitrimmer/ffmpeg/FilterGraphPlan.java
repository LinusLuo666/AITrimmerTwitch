package com.aitrimmer.ffmpeg;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

final class FilterGraphPlan implements FfmpegCommandPlan {
    private final List<String> args;

    FilterGraphPlan(List<String> args) {
        this.args = List.copyOf(Objects.requireNonNull(args, "args"));
    }

    @Override
    public List<String> command(Path scriptPath) {
        return args;
    }
}
