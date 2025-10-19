package com.aitrimmertwitch.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 任务核心数据模型。
 */
public class VideoEditTask {

	private UUID id;

	private String sourceVideoName;

	private List<TaskSegment> segments = new ArrayList<>();

	private String qualityProfile;

	private TaskStatus status;

	private Instant createdAt;

	private Instant updatedAt;

	private String outputFileName;

	private List<String> executionPreview = new ArrayList<>();

	public VideoEditTask() {
	}

	@JsonCreator
	public VideoEditTask(@JsonProperty("id") UUID id, @JsonProperty("sourceVideoName") String sourceVideoName,
			@JsonProperty("segments") List<TaskSegment> segments, @JsonProperty("qualityProfile") String qualityProfile,
			@JsonProperty("status") TaskStatus status, @JsonProperty("createdAt") Instant createdAt,
			@JsonProperty("updatedAt") Instant updatedAt, @JsonProperty("outputFileName") String outputFileName,
			@JsonProperty("executionPreview") List<String> executionPreview) {
		this.id = id;
		this.sourceVideoName = sourceVideoName;
		if (segments != null) {
			this.segments = new ArrayList<>(segments);
		}
		this.qualityProfile = qualityProfile;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.outputFileName = outputFileName;
		if (executionPreview != null) {
			this.executionPreview = new ArrayList<>(executionPreview);
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getSourceVideoName() {
		return sourceVideoName;
	}

	public void setSourceVideoName(String sourceVideoName) {
		this.sourceVideoName = sourceVideoName;
	}

	public List<TaskSegment> getSegments() {
		return segments;
	}

	public void setSegments(List<TaskSegment> segments) {
		this.segments = segments != null ? new ArrayList<>(segments) : new ArrayList<>();
	}

	public String getQualityProfile() {
		return qualityProfile;
	}

	public void setQualityProfile(String qualityProfile) {
		this.qualityProfile = qualityProfile;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public List<String> getExecutionPreview() {
		return executionPreview;
	}

	public void setExecutionPreview(List<String> executionPreview) {
		this.executionPreview = executionPreview != null ? new ArrayList<>(executionPreview) : new ArrayList<>();
	}

	public void markUpdated() {
		this.updatedAt = Instant.now();
	}

	public void touchCreated() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void ensureIntegrity() {
		Objects.requireNonNull(id, "任务 id 不可为 null");
		Objects.requireNonNull(sourceVideoName, "sourceVideoName 不可为 null");
		Objects.requireNonNull(status, "status 不可为 null");
		Objects.requireNonNull(createdAt, "createdAt 不可为 null");
		Objects.requireNonNull(updatedAt, "updatedAt 不可为 null");
	}

}
