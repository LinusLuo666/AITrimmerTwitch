package com.example.backend.dto;

import java.time.Instant;

public record TaskDto(Long id, String name, TaskStatus status, Instant createdAt) {
}
