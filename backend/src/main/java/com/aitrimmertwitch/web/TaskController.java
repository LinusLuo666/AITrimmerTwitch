package com.aitrimmertwitch.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.aitrimmertwitch.web.mapper.TaskViewMapper;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/tasks")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173", "http://[::1]:5173" }, allowCredentials = "true")
public class TaskController {

	private final TaskService taskService;

	private final TaskViewMapper mapper;

	public TaskController(TaskService taskService, TaskViewMapper mapper) {
		this.taskService = taskService;
		this.mapper = mapper;
	}

	@PostMapping
	public ResponseEntity<TaskView> createTask(@Valid @RequestBody CreateTaskRequest request) {
		VideoEditTask task = taskService.createTask(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toView(task));
	}

	@GetMapping
	public List<TaskView> listTasks() {
		return taskService.listTasks().stream().map(mapper::toView).toList();
	}

	@PostMapping("/{id}/approve")
	public TaskView approve(@PathVariable UUID id) {
		return mapper.toView(taskService.approve(id));
	}

	@PostMapping("/{id}/pause")
	public TaskView pause(@PathVariable UUID id) {
		return mapper.toView(taskService.pause(id));
	}

	@PostMapping("/{id}/cancel")
	public TaskView cancel(@PathVariable UUID id) {
		return mapper.toView(taskService.cancel(id));
	}

	@PostMapping("/{id}/running")
	public TaskView markRunning(@PathVariable UUID id) {
		return mapper.toView(taskService.markRunning(id));
	}

	@PostMapping("/{id}/completed")
	public TaskView markCompleted(@PathVariable UUID id) {
		return mapper.toView(taskService.markCompleted(id));
	}

	@PostMapping("/{id}/failed")
	public TaskView markFailed(@PathVariable UUID id) {
		return mapper.toView(taskService.markFailed(id));
	}
}
