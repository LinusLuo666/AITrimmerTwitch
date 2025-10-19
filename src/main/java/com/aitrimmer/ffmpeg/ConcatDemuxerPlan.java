package com.aitrimmer.ffmpeg;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class ConcatDemuxerPlan implements FfmpegCommandPlan {
    private final String scriptContent;
    private final List<String> prefixArgs;
    private final List<String> suffixArgs;

    ConcatDemuxerPlan(String scriptContent, List<String> prefixArgs, List<String> suffixArgs) {
        this.scriptContent = Objects.requireNonNull(scriptContent, "scriptContent");
        this.prefixArgs = List.copyOf(prefixArgs);
        this.suffixArgs = List.copyOf(suffixArgs);
    }

    @Override
    public List<String> command(Path scriptPath) {
        Objects.requireNonNull(scriptPath, "scriptPath");
        List<String> args = new ArrayList<>(prefixArgs.size() + suffixArgs.size() + 1);
        args.addAll(prefixArgs);
        args.add(scriptPath.toString());
        args.addAll(suffixArgs);
        return args;
    }

    @Override
    public Optional<String> concatScript() {
        return Optional.of(scriptContent);
    }
}
