package com.aitrimmertwitch.ffmpeg;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aitrimmertwitch.config.VideoProcessorProperties;
import com.aitrimmertwitch.config.VideoProcessorProperties.QualityProfile;
import com.aitrimmertwitch.model.TaskSegment;
import com.aitrimmertwitch.model.VideoEditTask;

/**
 * 根据任务内容生成对应的 FFmpeg 命令脚本（仅供审核）。
 */
@Component
public class FfmpegCommandPlanner {

	public List<String> buildPlan(VideoEditTask task, VideoProcessorProperties properties, QualityProfile qualityProfile) {
		List<String> commands = new ArrayList<>();
		Path workspace = properties.getWorkspacePath();
		Path processingDir = workspace.resolve("processing").resolve(task.getId().toString());
		Path inputPath = workspace.resolve(task.getSourceVideoName());
		Path concatList = processingDir.resolve("segments.txt");
		Path outputPath = workspace.resolve(task.getOutputFileName());
		String ffmpeg = quote(properties.getFfmpegBinary());

		commands.add(powershell(String.format("New-Item -ItemType Directory -Force -Path %s", quote(processingDir))));

		List<String> concatEntries = new ArrayList<>();

		int index = 1;
		for (TaskSegment segment : task.getSegments()) {
			Path segmentPath = processingDir.resolve(String.format(Locale.ROOT, "segment_%02d.mp4", index));
			String cmd = "%s -hide_banner -y -ss %s -to %s -i %s -c copy %s".formatted(ffmpeg, segment.getStart(),
					segment.getEnd(), quote(inputPath), quote(segmentPath));
			commands.add(cmd);
			concatEntries.add("file " + quote(segmentPath));
			index++;
		}

		commands.add(powershell(
				"@\"\n" + String.join(System.lineSeparator(), concatEntries) + "\n\"@ | Set-Content -Encoding UTF8 "
						+ quote(concatList)));

		String encodeCommand = "%s -hide_banner -y -safe 0 -f concat -i %s -c:v libx264 -preset %s -crf %d -b:v %s -c:a aac -b:a %s %s"
			.formatted(ffmpeg, quote(concatList), qualityProfile.preset(), qualityProfile.crf(),
					qualityProfile.videoBitrate(), qualityProfile.audioBitrate(), quote(outputPath));
		commands.add(encodeCommand);

		return commands;
	}

	private String powershell(String script) {
		return "powershell -NoProfile -Command \"" + script.replace("\"", "`\"") + "\"";
	}

	private String quote(Path path) {
		return "\"" + path.toString().replace("\"", "\\\"") + "\"";
	}

	public static String generateOutputFileName(String prefix, String originalName, UUID id) {
		String base = originalName;
		String extension = ".mp4";
		int idx = originalName.lastIndexOf('.');
		if (idx >= 0) {
			base = originalName.substring(0, idx);
			extension = originalName.substring(idx);
		}
		String sanitized = base.replaceAll("[^a-zA-Z0-9-_]", "_");
		return prefix + sanitized + "_" + id + extension;
	}
}
