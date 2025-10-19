package com.aitrimmertwitch.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aitrimmertwitch.config.VideoProcessorProperties;
import com.aitrimmertwitch.config.VideoProcessorProperties.QualityProfile;
import com.aitrimmertwitch.ffmpeg.FfmpegCommandPlanner;
import com.aitrimmertwitch.model.TaskSegment;
import com.aitrimmertwitch.model.TaskStatus;
import com.aitrimmertwitch.model.VideoEditTask;
import com.aitrimmertwitch.repository.TaskRepository;
import com.aitrimmertwitch.web.dto.CreateTaskRequest;

@Service
public class TaskService {

	private final TaskRepository repository;

	private final VideoProcessorProperties properties;

	private final FfmpegCommandPlanner planner;

	public TaskService(TaskRepository repository, VideoProcessorProperties properties, FfmpegCommandPlanner planner) {
		this.repository = repository;
		this.properties = properties;
		this.planner = planner;
	}

	public VideoEditTask createTask(CreateTaskRequest request) {
		validateVideoName(request.videoName());
		List<TaskSegment> segments = request.segments().stream()
			.map(segment -> new TaskSegment(segment.start(), segment.end()))
			.collect(Collectors.toList());
		validateSegments(segments);

		QualityProfile profile = properties.requireQuality(request.quality());

		VideoEditTask task = new VideoEditTask();
		task.setId(UUID.randomUUID());
		task.setSourceVideoName(request.videoName());
		task.setSegments(segments);
		task.setQualityProfile(request.quality());
		task.setStatus(TaskStatus.PENDING_APPROVAL);
		task.touchCreated();

		String outputFileName = FfmpegCommandPlanner.generateOutputFileName(properties.getOutputPrefix(),
				request.videoName(), task.getId());
		task.setOutputFileName(outputFileName);

		List<String> commands = planner.buildPlan(task, properties, profile);
		task.setExecutionPreview(commands);
		task.ensureIntegrity();

		repository.save(task);
		return task;
	}

	public List<VideoEditTask> listTasks() {
		return repository.findAll();
	}

	public VideoEditTask approve(UUID id) {
		return updateStatus(id, TaskStatus.APPROVED);
	}

	public VideoEditTask pause(UUID id) {
		return updateStatus(id, TaskStatus.PAUSED);
	}

	public VideoEditTask cancel(UUID id) {
		return updateStatus(id, TaskStatus.CANCELLED);
	}

	public VideoEditTask markRunning(UUID id) {
		return updateStatus(id, TaskStatus.RUNNING);
	}

	public VideoEditTask markCompleted(UUID id) {
		return updateStatus(id, TaskStatus.COMPLETED);
	}

	public VideoEditTask markFailed(UUID id) {
		return updateStatus(id, TaskStatus.FAILED);
	}

	private VideoEditTask updateStatus(UUID id, TaskStatus status) {
		VideoEditTask task = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("找不到指定任務：" + id));
		task.setStatus(status);
		task.markUpdated();
		repository.save(task);
		return task;
	}

	private void validateVideoName(String videoName) {
		if (!StringUtils.hasText(videoName)) {
			throw new IllegalArgumentException("影片名稱不可為空");
		}

		Path workspace = properties.getWorkspacePath().toAbsolutePath().normalize();

		Path resolved = workspace.resolve(videoName).normalize();
		if (!resolved.startsWith(workspace)) {
			throw new IllegalArgumentException("影片名稱包含非法路徑片段：" + videoName);
		}

		if (properties.isLockEditedOutputs() && videoName.startsWith(properties.getOutputPrefix())) {
			throw new IllegalArgumentException("偵測到已處理過的輸出檔案，為避免重複編輯已拒絕此次操作。");
		}

		if (!Files.exists(resolved)) {
			throw new IllegalArgumentException("工作區內找不到指定影片：" + resolved);
		}
	}

	private void validateSegments(List<TaskSegment> segments) {
		if (segments.isEmpty()) {
			throw new IllegalArgumentException("至少需要配置一個時間片段。");
		}
		for (TaskSegment segment : segments) {
			Duration start = segment.startAsDuration();
			Duration end = segment.endAsDuration();
			if (!end.minus(start).isPositive()) {
				throw new IllegalArgumentException(
						"時間片段結束時間必須晚於開始時間：" + segment.getStart() + " -> " + segment.getEnd());
			}
		}
	}
}
