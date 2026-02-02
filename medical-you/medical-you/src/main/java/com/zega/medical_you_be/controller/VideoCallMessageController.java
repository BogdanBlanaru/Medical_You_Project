package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.VideoCallMessageDto;
import com.zega.medical_you_be.service.VideoCallMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-call-messages")
@RequiredArgsConstructor
public class VideoCallMessageController {

    private final VideoCallMessageService messageService;

    /**
     * Save a single message.
     */
    @PostMapping
    public ResponseEntity<VideoCallMessageDto> saveMessage(@RequestBody VideoCallMessageDto messageDto) {
        return ResponseEntity.ok(messageService.saveMessage(messageDto));
    }

    /**
     * Save multiple messages (batch save at end of call).
     */
    @PostMapping("/batch")
    public ResponseEntity<List<VideoCallMessageDto>> saveMessages(@RequestBody List<VideoCallMessageDto> messages) {
        return ResponseEntity.ok(messageService.saveMessages(messages));
    }

    /**
     * Get all messages for a specific room/call.
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<VideoCallMessageDto>> getMessagesByRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(messageService.getMessagesByRoomId(roomId));
    }

    /**
     * Get rooms that a user has participated in.
     */
    @GetMapping("/user/{userId}/rooms")
    public ResponseEntity<List<String>> getRoomsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getRoomsForUser(userId));
    }
}
