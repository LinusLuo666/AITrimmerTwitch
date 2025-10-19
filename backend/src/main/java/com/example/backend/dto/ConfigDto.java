package com.example.backend.dto;

public record ConfigDto(String defaultLanguage, int maxConcurrentTasks, boolean featureFlagsEnabled) {
}
