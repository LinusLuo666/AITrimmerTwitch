package com.aitrimmertwitch.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record CreateTaskRequest(@NotBlank(message = "請提供影片名稱") String videoName,
		@NotEmpty(message = "至少需要一個時間片段") List<@Valid Segment> segments,
		@NotBlank(message = "請指定畫質設定") String quality, boolean autoApprove) {

	public record Segment(
			@NotBlank(message = "開始時間不可為空") @Pattern(regexp = "^[0-9]{1,2}(:[0-9]{2}){1,2}$", message = "請使用 HH:mm:ss 或 mm:ss 格式") String start,
			@NotBlank(message = "結束時間不可為空") @Pattern(regexp = "^[0-9]{1,2}(:[0-9]{2}){1,2}$", message = "請使用 HH:mm:ss 或 mm:ss 格式") String end) {
	}
}
