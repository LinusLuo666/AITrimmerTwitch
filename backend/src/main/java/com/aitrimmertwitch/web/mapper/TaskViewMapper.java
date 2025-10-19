package com.aitrimmertwitch.web.mapper;

import org.springframework.stereotype.Component;

import com.aitrimmertwitch.model.VideoEditTask;
import com.aitrimmertwitch.web.dto.TaskView;

@Component
public class TaskViewMapper {

	public TaskView toView(VideoEditTask task) {
		return new TaskView(task.getId(), task.getSourceVideoName(), task.getStatus(), task.getSegments(),
				task.getQualityProfile(), task.getOutputFileName(), task.getExecutionPreview(), task.getCreatedAt(),
				task.getUpdatedAt());
	}
}
