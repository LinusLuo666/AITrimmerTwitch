package com.aitrimmer.ffmpeg;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FfmpegCommandBuilderTest {

    private final CompressionPresetConfig presetConfig = new CompressionPresetConfig();
    private final FfmpegCommandBuilder builder = new FfmpegCommandBuilder(presetConfig);

    @Test
    void buildsConcatScriptPlan() {
        List<VideoSegment> segments = List.of(
                VideoSegment.builder()
                        .source(Path.of("/videos/clip1.mp4"))
                        .start(Duration.ofSeconds(5))
                        .end(Duration.ofSeconds(15))
                        .build(),
                VideoSegment.builder()
                        .source(Path.of("/videos/clip2.mp4"))
                        .build()
        );

        FfmpegCommandPlan plan = builder.build(
                segments,
                Path.of("/output/final.mp4"),
                null,
                FfmpegCommandBuilder.Strategy.CONCAT_SCRIPT
        );

        assertTrue(plan.concatScript().isPresent());
        String script = plan.concatScript().orElseThrow();
        assertTrue(script.contains("file '/videos/clip1.mp4'"));
        assertTrue(script.contains("inpoint 5.0"));
        assertTrue(script.contains("outpoint 15.0"));
        assertTrue(script.contains("file '/videos/clip2.mp4'"));

        List<String> command = plan.command(Path.of("concat.txt"));
        assertEquals(List.of(
                "ffmpeg", "-y", "-hide_banner", "-safe", "0", "-f", "concat", "-i", "concat.txt",
                "-c:v", "libx264", "-preset", "medium", "-crf", "23", "-c:a", "aac", "-b:a", "128k", "/output/final.mp4"
        ), command);
    }

    @Test
    void buildsFilterComplexPlan() {
        List<VideoSegment> segments = List.of(
                VideoSegment.builder()
                        .source(Path.of("clip1.mp4"))
                        .start(Duration.ofSeconds(2))
                        .end(Duration.ofSeconds(4))
                        .build(),
                VideoSegment.builder()
                        .source(Path.of("clip2.mp4"))
                        .build()
        );

        FfmpegCommandPlan plan = builder.build(
                segments,
                Path.of("final.mp4"),
                null,
                FfmpegCommandBuilder.Strategy.FILTER_COMPLEX
        );

        assertTrue(plan.concatScript().isEmpty());
        List<String> command = plan.command(null);
        assertEquals("ffmpeg", command.get(0));
        assertTrue(command.contains("-filter_complex"));
        int index = command.indexOf("-filter_complex");
        String filterGraph = command.get(index + 1);
        assertTrue(filterGraph.contains("trim=start=2.0:end=4.0"));
        assertTrue(filterGraph.contains("concat=n=2:v=1:a=1[vout][aout]"));
        assertTrue(command.containsAll(List.of("-map", "[vout]")));
        assertTrue(command.containsAll(List.of("-map", "[aout]")));
        assertEquals("final.mp4", command.get(command.size() - 1));
    }
}
