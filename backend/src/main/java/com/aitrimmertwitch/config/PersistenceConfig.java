package com.aitrimmertwitch.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aitrimmertwitch.repository.FileTaskRepository;
import com.aitrimmertwitch.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class PersistenceConfig {

	@Bean
	public TaskRepository taskRepository(VideoProcessorProperties properties, ObjectMapper objectMapper) {
		ObjectMapper mapper = objectMapper.copy().findAndRegisterModules();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		Path storage = properties.getWorkspacePath().resolve("tasks").resolve("tasks.json");
		return new FileTaskRepository(storage, mapper);
	}
}
