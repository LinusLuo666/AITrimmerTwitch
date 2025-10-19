package com.example.backend.service;

import com.example.backend.dto.TaskDto;
import com.example.backend.dto.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final List<TaskDto> demoTasks = List.of(
            new TaskDto(1L, "Transcode highlight", TaskStatus.RUNNING, Instant.now().minusSeconds(300)),
            new TaskDto(2L, "Upload to Twitch", TaskStatus.QUEUED, Instant.now().minusSeconds(120)),
            new TaskDto(3L, "Generate thumbnail", TaskStatus.COMPLETED, Instant.now().minusSeconds(900))
    );

    public List<TaskDto> findAll() {
        return demoTasks;
    }

    public Optional<TaskDto> findById(Long id) {
        return demoTasks.stream()
                .filter(task -> task.id().equals(id))
                .findFirst();
    }
}
