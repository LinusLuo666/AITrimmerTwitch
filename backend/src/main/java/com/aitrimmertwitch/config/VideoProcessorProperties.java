package com.aitrimmertwitch.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 管理与工作区、输出前缀、FFmpeg 配置相关的可调整参数。
 */
@Validated
@ConfigurationProperties(prefix = "video.processor")
public class VideoProcessorProperties {

	private Path workspacePath = Path.of("C:/AITrimmer/workspace");

	private Path ffmpegBinary = Path.of("C:/Program Files/ffmpeg/bin/ffmpeg.exe");

	private String outputPrefix = "AITRIM_";

	private Map<String, QualityProfile> qualities = defaultQualities();

	private boolean lockEditedOutputs = true;

	public Path getWorkspacePath() {
		return workspacePath;
	}

	public void setWorkspacePath(Path workspacePath) {
		this.workspacePath = workspacePath;
	}

	public Path getFfmpegBinary() {
		return ffmpegBinary;
	}

	public void setFfmpegBinary(Path ffmpegBinary) {
		this.ffmpegBinary = ffmpegBinary;
	}

	public String getOutputPrefix() {
		return outputPrefix;
	}

	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}

	public Map<String, QualityProfile> getQualities() {
		return qualities;
	}

	public void setQualities(Map<String, QualityProfile> qualities) {
		if (qualities != null && !qualities.isEmpty()) {
			this.qualities = new LinkedHashMap<>(qualities);
		}
	}

	public boolean isLockEditedOutputs() {
		return lockEditedOutputs;
	}

	public void setLockEditedOutputs(boolean lockEditedOutputs) {
		this.lockEditedOutputs = lockEditedOutputs;
	}

	private Map<String, QualityProfile> defaultQualities() {
		Map<String, QualityProfile> defaults = new LinkedHashMap<>();
		defaults.put("low", new QualityProfile("1500k", "96k", "veryfast", 28));
		defaults.put("medium", new QualityProfile("3500k", "128k", "medium", 23));
		defaults.put("high", new QualityProfile("6000k", "192k", "slow", 20));
		return defaults;
	}

	public QualityProfile requireQuality(String name) {
		QualityProfile profile = qualities.get(name);
		if (profile == null) {
			throw new IllegalArgumentException("未找到画质配置：" + name);
		}
		return profile;
	}

	public record QualityProfile(String videoBitrate, String audioBitrate, String preset, Integer crf) {

		public QualityProfile {
			Objects.requireNonNull(videoBitrate, "videoBitrate 不可为 null");
			Objects.requireNonNull(audioBitrate, "audioBitrate 不可为 null");
			Objects.requireNonNull(preset, "preset 不可为 null");
		}
	}

}
