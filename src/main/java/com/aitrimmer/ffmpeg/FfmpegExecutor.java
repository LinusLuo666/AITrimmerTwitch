package com.aitrimmer.ffmpeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Executes FFmpeg commands, streams logs, and updates task status.
 */
public final class FfmpegExecutor {

    public int execute(
            FfmpegCommandPlan plan,
            ProcessingTask task,
            Path workingDirectory,
            Consumer<String> logConsumer
    ) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(task, "task");
        Consumer<String> consumer = logConsumer != null ? logConsumer : line -> {};
        Path scriptFile = null;
        try {
            Optional<String> concatScript = plan.concatScript();
            if (concatScript.isPresent()) {
                scriptFile = Files.createTempFile("ffmpeg_concat_", ".txt");
                Files.writeString(scriptFile, concatScript.get(), StandardCharsets.UTF_8);
            }

            List<String> command = plan.command(scriptFile);
            ProcessBuilder builder = new ProcessBuilder(command);
            if (workingDirectory != null) {
                builder.directory(workingDirectory.toFile());
            }
            builder.redirectErrorStream(true);
            task.setStatus(TaskStatus.RUNNING);
            Process process = builder.start();
            streamLogs(process, task, consumer);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                task.setStatus(TaskStatus.SUCCEEDED);
            } else {
                task.setStatus(TaskStatus.FAILED);
            }
            return exitCode;
        } catch (IOException e) {
            task.setStatus(TaskStatus.FAILED);
            throw new FfmpegExecutionException("Failed to execute FFmpeg", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus(TaskStatus.FAILED);
            throw new FfmpegExecutionException("FFmpeg execution was interrupted", e);
        } finally {
            if (scriptFile != null) {
                try {
                    Files.deleteIfExists(scriptFile);
                } catch (IOException ignored) {
                    // Best effort cleanup.
                }
            }
        }
    }

    private void streamLogs(Process process, ProcessingTask task, Consumer<String> consumer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
                task.appendLog(line);
            }
        }
    }
}
