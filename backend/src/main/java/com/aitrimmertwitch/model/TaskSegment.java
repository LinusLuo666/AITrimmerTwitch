package com.aitrimmertwitch.model;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 任務中的單一時間片段。以 HH:mm:ss 或 mm:ss 格式表示。
 */
public class TaskSegment {

	private String start;

	private String end;

	public TaskSegment() {
	}

	@JsonCreator
	public TaskSegment(@JsonProperty("start") String start, @JsonProperty("end") String end) {
		this.start = start;
		this.end = end;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public Duration startAsDuration() {
		return parseTimecode(start);
	}

	public Duration endAsDuration() {
		return parseTimecode(end);
	}

	private Duration parseTimecode(String value) {
		Objects.requireNonNull(value, "timecode 不可為 null");
		try {
			if (value.contains(":")) {
				String[] parts = value.split(":");
				if (parts.length == 3) {
					return Duration.ofHours(Long.parseLong(parts[0]))
						.plusMinutes(Long.parseLong(parts[1]))
						.plusSeconds(Long.parseLong(parts[2]));
				}
				if (parts.length == 2) {
					return Duration.ofMinutes(Long.parseLong(parts[0])).plusSeconds(Long.parseLong(parts[1]));
				}
			}
			return Duration.ofSeconds(Long.parseLong(value));
		}
		catch (NumberFormatException ex) {
			throw new DateTimeParseException("無法解析時間碼：" + value, value, 0, ex);
		}
	}
}
