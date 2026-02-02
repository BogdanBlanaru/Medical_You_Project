package com.zega.medical_you_be.service;

import com.zega.medical_you_be.mapper.ChatLogMapper;
import com.zega.medical_you_be.model.dto.ChatLogDto;
import com.zega.medical_you_be.model.entity.ChatLog;
import com.zega.medical_you_be.repo.ChatLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatLogServiceImpl implements ChatLogService {

    private final ChatLogRepo chatRepo;
    private final ChatLogMapper chatMapper;

    @Override
    public List<ChatLogDto> getAllChatLogs() {
        List<ChatLog> chatLogs = chatRepo.findAll();
        return chatLogs.stream().map(log -> ChatLogDto.builder()
                .id(log.getId())
                .prognosis(log.getPrognosis())
                .description(log.getDescription())
                .symptoms(log.getSymptoms())
                .precautions(log.getPrecautions() != null ? Arrays.asList(log.getPrecautions()) : List.of())
                .complications(log.getComplications() != null ? Arrays.asList(log.getComplications()) : List.of())
                .severity(log.getSeverity())
                .createdAt(log.getCreatedAt())
                .build()).toList();
    }

    @Override
    public boolean saveChatLog(ChatLogDto chatLogDto) {
        if (chatLogDto == null) {
            return false;
        }

        chatRepo.save(ChatLog.builder()
                .id(chatLogDto.getId())
                .prognosis(chatLogDto.getPrognosis())
                .description(chatLogDto.getDescription())
                .symptoms(chatLogDto.getSymptoms())
                .precautions(chatLogDto.getPrecautions() != null ? chatLogDto.getPrecautions().toArray(new String[0]) : new String[0])
                .complications(chatLogDto.getComplications() != null ? chatLogDto.getComplications().toArray(new String[0]) : new String[0])
                .severity(chatLogDto.getSeverity())
                .isDeleted(false)
                .build());
        return true;
    }

    @Override
    public boolean updateChatLog(ChatLogDto chatLogDto) {
        Optional<ChatLog> optionalChatLog = chatRepo.findById(chatLogDto.getId());
        if (optionalChatLog.isPresent()) {
            chatRepo.save(ChatLog.builder()
                    .id(chatLogDto.getId())
                    .prognosis(chatLogDto.getPrognosis())
                    .description(chatLogDto.getDescription())
                    .symptoms(chatLogDto.getSymptoms())
                    .precautions(chatLogDto.getPrecautions() != null ? chatLogDto.getPrecautions().toArray(new String[0]) : new String[0])
                    .complications(chatLogDto.getComplications() != null ? chatLogDto.getComplications().toArray(new String[0]) : new String[0])
                    .severity(chatLogDto.getSeverity())
                    .isDeleted(false)
                    .build());
            return true;
        }
        return false;
    }
}
