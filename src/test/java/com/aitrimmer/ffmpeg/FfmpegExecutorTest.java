package com.aitrimmer.ffmpeg;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FfmpegExecutorTest {

    private final FfmpegExecutor executor = new FfmpegExecutor();

    @Test
    void streamsLogsAndMarksSuccess() {
        FfmpegCommandPlan plan = new FfmpegCommandPlan() {
            @Override
            public List<String> command(Path scriptPath) {
                return List.of("bash", "-lc", "echo ready && echo done");
            }
        };
        ProcessingTask task = new ProcessingTask(UUID.randomUUID(), "log-success");
        List<String> collected = new ArrayList<>();

        int exitCode = executor.execute(plan, task, null, collected::add);

        assertEquals(0, exitCode);
        assertEquals(TaskStatus.SUCCEEDED, task.getStatus());
        assertEquals(List.of("ready", "done"), collected);
        assertEquals(collected, task.getLogs());
    }

    @Test
    void capturesFailures() {
        FfmpegCommandPlan plan = new FfmpegCommandPlan() {
            @Override
            public List<String> command(Path scriptPath) {
                return List.of("bash", "-lc", "echo oops >&2; exit 5");
            }
        };
        ProcessingTask task = new ProcessingTask(UUID.randomUUID(), "log-failure");
        List<String> collected = new ArrayList<>();

        int exitCode = executor.execute(plan, task, null, collected::add);

        assertEquals(5, exitCode);
        assertEquals(TaskStatus.FAILED, task.getStatus());
        assertTrue(collected.contains("oops"));
    }

    @Test
    void writesConcatScriptWhenProvided() {
        FfmpegCommandPlan plan = new FfmpegCommandPlan() {
            @Override
            public List<String> command(Path scriptPath) {
                assertNotNull(scriptPath);
                return List.of("bash", "-lc", "cat '" + scriptPath + "'");
            }

            @Override
            public Optional<String> concatScript() {
                return Optional.of("file 'a.mp4'\n");
            }
        };

        ProcessingTask task = new ProcessingTask(UUID.randomUUID(), "concat-script");
        List<String> collected = new ArrayList<>();

        int exitCode = executor.execute(plan, task, null, collected::add);

        assertEquals(0, exitCode);
        assertEquals(TaskStatus.SUCCEEDED, task.getStatus());
        assertTrue(collected.stream().anyMatch(line -> line.contains("file 'a.mp4'")));
    }
}
