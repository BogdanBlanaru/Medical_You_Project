package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLogRepo extends JpaRepository<ChatLog, Long> {
}
