package com.aitrimmertwitch.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.aitrimmertwitch.model.VideoEditTask;

public interface TaskRepository {

	VideoEditTask save(VideoEditTask task);

	Optional<VideoEditTask> findById(UUID id);

	List<VideoEditTask> findAll();

	void delete(UUID id);
}
