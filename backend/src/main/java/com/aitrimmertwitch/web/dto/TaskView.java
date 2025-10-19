package com.aitrimmertwitch.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.aitrimmertwitch.model.TaskSegment;
import com.aitrimmertwitch.model.TaskStatus;

public record TaskView(UUID id, String videoName, TaskStatus status, List<TaskSegment> segments, String quality,
		String outputFileName, List<String> executionPreview, Instant createdAt, Instant updatedAt) {
}
