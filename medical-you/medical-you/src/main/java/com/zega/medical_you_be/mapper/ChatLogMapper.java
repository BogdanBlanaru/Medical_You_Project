package com.zega.medical_you_be.mapper;

import com.zega.medical_you_be.model.dto.ChatLogDto;
import com.zega.medical_you_be.model.entity.ChatLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatLogMapper {

    ChatLog toEntity(ChatLogDto chatLogDto);
}
