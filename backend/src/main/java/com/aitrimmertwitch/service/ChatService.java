package com.aitrimmertwitch.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aitrimmertwitch.web.dto.ChatRequest;
import com.aitrimmertwitch.web.dto.ChatResponse;
import com.aitrimmertwitch.web.dto.CreateTaskRequest;
import com.aitrimmertwitch.web.dto.TaskView;
import com.aitrimmertwitch.web.mapper.TaskViewMapper;

@Service
public class ChatService {

	private static final Set<String> VIDEO_KEYS = Set.of("视频", "视频名称", "video", "文件");

	private static final Set<String> SEGMENTS_KEYS = Set.of("片段", "时间", "时间段", "segments");

	private static final Set<String> QUALITY_KEYS = Set.of("画质", "质量", "quality");

	private static final Set<String> AUTO_KEYS = Set.of("自动批准", "自动审批", "auto", "自动通过");

	private final TaskService taskService;

	private final TaskViewMapper mapper;

	public ChatService(TaskService taskService, TaskViewMapper mapper) {
		this.taskService = taskService;
		this.mapper = mapper;
	}

	public ChatResponse handle(ChatRequest request) {
		String message = request.message();
		Map<String, String> tokens = tokenize(message);

		String video = findValue(tokens, VIDEO_KEYS);
		if (!StringUtils.hasText(video)) {
			throw new IllegalArgumentException("未识别到视频名称，请使用格式：视频=demo.mp4;");
		}

		String segmentsValue = findValue(tokens, SEGMENTS_KEYS);
		if (!StringUtils.hasText(segmentsValue)) {
			throw new IllegalArgumentException("未识别到时间片段，请使用格式：片段=00:00:05-00:00:20,00:01:00-00:02:00;");
		}

		List<CreateTaskRequest.Segment> segments = parseSegments(segmentsValue);

		String quality = findValue(tokens, QUALITY_KEYS);
		if (!StringUtils.hasText(quality)) {
			quality = "medium";
		}

		boolean autoApprove = parseBoolean(findValue(tokens, AUTO_KEYS));

		CreateTaskRequest createRequest = new CreateTaskRequest(video, segments, quality.toLowerCase(Locale.ROOT),
				autoApprove);
		var task = taskService.createTask(createRequest);

		if (autoApprove) {
			task = taskService.approve(task.getId());
		}

		TaskView view = mapper.toView(task);
		String reply = buildReply(view, autoApprove);
		return new ChatResponse(reply, view);
	}

	private String buildReply(TaskView view, boolean autoApprove) {
		StringBuilder builder = new StringBuilder();
		builder.append("已根据指令创建任务：").append(view.id()).append("。");
		if (autoApprove) {
			builder.append("已自动标记为已批准。");
		}
		builder.append("生成的输出文件：").append(view.outputFileName()).append("。");
		return builder.toString();
	}

	private Map<String, String> tokenize(String message) {
		if (!StringUtils.hasText(message)) {
			throw new IllegalArgumentException("请输入有效的指令。例如：视频=demo.mp4; 片段=00:00:05-00:00:20; 画质=medium;");
		}
		Map<String, String> tokens = new HashMap<>();
		Arrays.stream(message.split(";"))
			.map(String::trim)
			.filter(token -> !token.isEmpty())
			.forEach(token -> {
				String normalized = token.replace("：", "=");
				int idx = normalized.indexOf('=');
				if (idx > 0) {
					String key = normalized.substring(0, idx).trim().toLowerCase(Locale.ROOT);
					String value = normalized.substring(idx + 1).trim();
					tokens.put(key, value);
				}
			});
		return tokens;
	}

	private String findValue(Map<String, String> tokens, Set<String> keys) {
		for (String key : keys) {
			String value = tokens.get(key.toLowerCase(Locale.ROOT));
			if (StringUtils.hasText(value)) {
				return value;
			}
		}
		return null;
	}

	private List<CreateTaskRequest.Segment> parseSegments(String raw) {
		String[] parts = raw.split(",");
		List<CreateTaskRequest.Segment> segments = new ArrayList<>();
		for (String part : parts) {
			String cleaned = part.trim();
			if (cleaned.isEmpty()) {
				continue;
			}
			String normalized = cleaned.replace("到", "-");
			int idx = normalized.indexOf('-');
			if (idx <= 0 || idx == normalized.length() - 1) {
				throw new IllegalArgumentException("时间片段格式有误：" + cleaned + "，请使用开始-结束格式。");
			}
			String start = normalized.substring(0, idx).trim();
			String end = normalized.substring(idx + 1).trim();
			segments.add(new CreateTaskRequest.Segment(start, end));
		}
		if (segments.isEmpty()) {
			throw new IllegalArgumentException("时间片段无法识别，请提供至少一个有效的开始和结束时间。");
		}
		return segments;
	}

	private boolean parseBoolean(String value) {
		if (!StringUtils.hasText(value)) {
			return false;
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return Objects.equals(normalized, "true") || Objects.equals(normalized, "是") || Objects.equals(normalized, "yes")
				|| Objects.equals(normalized, "1");
	}
}
