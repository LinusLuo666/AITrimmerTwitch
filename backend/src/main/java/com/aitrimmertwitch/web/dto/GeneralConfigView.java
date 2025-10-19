package com.aitrimmertwitch.web.dto;

public record GeneralConfigView(String workspacePath, String ffmpegBinary, String outputPrefix, boolean lockEditedOutputs) {
}
