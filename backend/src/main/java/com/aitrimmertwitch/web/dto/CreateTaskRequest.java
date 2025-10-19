package com.aitrimmertwitch.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record CreateTaskRequest(@NotBlank(message = "请提供视频名称") String videoName,
		@NotEmpty(message = "至少需要一个时间片段") List<@Valid Segment> segments,
		@NotBlank(message = "请指定画质设置") String quality, boolean autoApprove) {

	public record Segment(
			@NotBlank(message = "开始时间不能为空") @Pattern(regexp = "^[0-9]{1,2}(:[0-9]{2}){1,2}$", message = "请使用 HH:mm:ss 或 mm:ss 格式") String start,
			@NotBlank(message = "结束时间不能为空") @Pattern(regexp = "^[0-9]{1,2}(:[0-9]{2}){1,2}$", message = "请使用 HH:mm:ss 或 mm:ss 格式") String end) {
	}
}
