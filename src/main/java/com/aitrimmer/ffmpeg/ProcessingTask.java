package com.aitrimmer.ffmpeg;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Minimal representation of an FFmpeg processing task.
 */
public final class ProcessingTask {
    private final UUID id;
    private final String description;
    private final List<String> logs = new ArrayList<>();
    private TaskStatus status = TaskStatus.PENDING;
    private Instant startedAt;
    private Instant completedAt;

    public ProcessingTask(UUID id, String description) {
        this.id = Objects.requireNonNull(id, "id");
        this.description = Objects.requireNonNull(description, "description");
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public synchronized TaskStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status");
        if (status == TaskStatus.RUNNING) {
            this.startedAt = Instant.now();
        }
        if (status == TaskStatus.SUCCEEDED || status == TaskStatus.FAILED) {
            this.completedAt = Instant.now();
        }
    }

    public synchronized void appendLog(String line) {
        logs.add(line);
    }

    public synchronized List<String> getLogs() {
        return List.copyOf(logs);
    }

    public synchronized Instant getStartedAt() {
        return startedAt;
    }

    public synchronized Instant getCompletedAt() {
        return completedAt;
    }
}
