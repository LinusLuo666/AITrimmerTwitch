package com.aitrimmertwitch.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank(message = "请输入聊天内容") String message) {
}
