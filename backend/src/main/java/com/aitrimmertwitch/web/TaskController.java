package com.aitrimmertwitch.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aitrimmertwitch.model.VideoEditTask;
import com.aitrimmertwitch.service.TaskService;
import com.aitrimmertwitch.web.dto.CreateTaskRequest;
import com.aitrimmertwitch.web.dto.TaskView;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	public ResponseEntity<TaskView> createTask(@Valid @RequestBody CreateTaskRequest request) {
		VideoEditTask task = taskService.createTask(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(toView(task));
	}

	@GetMapping
	public List<TaskView> listTasks() {
		return taskService.listTasks().stream().map(this::toView).toList();
	}

	@PostMapping("/{id}/approve")
	public TaskView approve(@PathVariable UUID id) {
		return toView(taskService.approve(id));
	}

	@PostMapping("/{id}/pause")
	public TaskView pause(@PathVariable UUID id) {
		return toView(taskService.pause(id));
	}

	@PostMapping("/{id}/cancel")
	public TaskView cancel(@PathVariable UUID id) {
		return toView(taskService.cancel(id));
	}

	@PostMapping("/{id}/running")
	public TaskView markRunning(@PathVariable UUID id) {
		return toView(taskService.markRunning(id));
	}

	@PostMapping("/{id}/completed")
	public TaskView markCompleted(@PathVariable UUID id) {
		return toView(taskService.markCompleted(id));
	}

	@PostMapping("/{id}/failed")
	public TaskView markFailed(@PathVariable UUID id) {
		return toView(taskService.markFailed(id));
	}

	private TaskView toView(VideoEditTask task) {
		return new TaskView(task.getId(), task.getSourceVideoName(), task.getStatus(), task.getSegments(),
				task.getQualityProfile(), task.getOutputFileName(), task.getExecutionPreview(), task.getCreatedAt(),
				task.getUpdatedAt());
	}
}
