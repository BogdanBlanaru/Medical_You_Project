package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.ChatLogDto;

import java.util.List;

public interface ChatLogService {

    List<ChatLogDto> getAllChatLogs();

    boolean saveChatLog(ChatLogDto chatLogDto);

    boolean updateChatLog(ChatLogDto chatLogDto);
}
