package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.ChatLogDto;
import com.zega.medical_you_be.service.ChatLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-logs")
@RequiredArgsConstructor
public class ChatLogController {

    private final ChatLogService chatService;

    @GetMapping
    public ResponseEntity<List<ChatLogDto>> getAllLogs() {
        return ResponseEntity.ok(chatService.getAllChatLogs());
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveChatLog(@RequestBody ChatLogDto chatLogDto) {
        if (chatService.saveChatLog(chatLogDto)) {
            return ResponseEntity.ok("Chat log saved!");
        }
        return ResponseEntity.internalServerError().build();
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateChatLog(@RequestBody ChatLogDto chatLogDto) {
        if (chatService.updateChatLog(chatLogDto)) {
            return ResponseEntity.ok("Updated chat log!");
        }
        return ResponseEntity.internalServerError().build();
    }
}
