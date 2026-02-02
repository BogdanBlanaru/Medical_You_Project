package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.VideoCallMessageDto;
import com.zega.medical_you_be.model.entity.VideoCallMessage;
import com.zega.medical_you_be.repo.VideoCallMessageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCallMessageServiceImpl implements VideoCallMessageService {

    private final VideoCallMessageRepo messageRepo;

    @Override
    @Transactional
    public VideoCallMessageDto saveMessage(VideoCallMessageDto messageDto) {
        VideoCallMessage message = mapToEntity(messageDto);
        message = messageRepo.save(message);
        log.info("Saved video call message for room: {}", messageDto.getRoomId());
        return mapToDto(message);
    }

    @Override
    @Transactional
    public List<VideoCallMessageDto> saveMessages(List<VideoCallMessageDto> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        List<VideoCallMessage> entities = messages.stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());

        List<VideoCallMessage> saved = messageRepo.saveAll(entities);
        log.info("Saved {} video call messages for room: {}",
                saved.size(), messages.get(0).getRoomId());

        return saved.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoCallMessageDto> getMessagesByRoomId(String roomId) {
        return messageRepo.findByRoomId(roomId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoomsForUser(Long userId) {
        return messageRepo.findRoomIdsByUserId(userId);
    }

    private VideoCallMessage mapToEntity(VideoCallMessageDto dto) {
        return VideoCallMessage.builder()
                .roomId(dto.getRoomId())
                .senderName(dto.getSenderName())
                .senderId(dto.getSenderId())
                .senderType(dto.getSenderType())
                .message(dto.getMessage())
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    private VideoCallMessageDto mapToDto(VideoCallMessage entity) {
        return VideoCallMessageDto.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .senderName(entity.getSenderName())
                .senderId(entity.getSenderId())
                .senderType(entity.getSenderType())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
