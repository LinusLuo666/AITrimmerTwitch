package com.aitrimmertwitch.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aitrimmertwitch.service.ChatService;
import com.aitrimmertwitch.web.dto.ChatRequest;
import com.aitrimmertwitch.web.dto.ChatResponse;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping
	public ChatResponse send(@Valid @RequestBody ChatRequest request) {
		return chatService.handle(request);
	}
}
