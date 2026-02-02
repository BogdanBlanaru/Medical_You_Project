package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.VideoCallMessageDto;

import java.util.List;

/**
 * Service for managing video call chat messages.
 */
public interface VideoCallMessageService {

    /**
     * Save a new chat message.
     */
    VideoCallMessageDto saveMessage(VideoCallMessageDto messageDto);

    /**
     * Save multiple messages at once (for batch save at end of call).
     */
    List<VideoCallMessageDto> saveMessages(List<VideoCallMessageDto> messages);

    /**
     * Get all messages for a specific room/call.
     */
    List<VideoCallMessageDto> getMessagesByRoomId(String roomId);

    /**
     * Get rooms that a user has participated in.
     */
    List<String> getRoomsForUser(Long userId);
}
