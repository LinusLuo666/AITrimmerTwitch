package com.aitrimmertwitch.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aitrimmertwitch.config.VideoProcessorProperties;
import com.aitrimmertwitch.web.dto.GeneralConfigView;
import com.aitrimmertwitch.web.dto.QualityView;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

	private final VideoProcessorProperties properties;

	public ConfigController(VideoProcessorProperties properties) {
		this.properties = properties;
	}

	@GetMapping("/qualities")
	public List<QualityView> qualities() {
		return properties.getQualities()
			.entrySet()
			.stream()
			.map(entry -> new QualityView(entry.getKey(), entry.getValue().videoBitrate(),
					entry.getValue().audioBitrate(), entry.getValue().preset(), entry.getValue().crf()))
			.toList();
	}

	@GetMapping("/general")
	public GeneralConfigView general() {
		return new GeneralConfigView(properties.getWorkspacePath().toString(),
				properties.getFfmpegBinary().toString(), properties.getOutputPrefix(), properties.isLockEditedOutputs());
	}
}
