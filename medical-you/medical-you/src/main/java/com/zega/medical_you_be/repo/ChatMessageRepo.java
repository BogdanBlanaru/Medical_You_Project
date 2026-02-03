package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.createdAt > :since AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<ChatMessage> findNewMessages(
            @Param("conversationId") Long conversationId,
            @Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = :now " +
           "WHERE m.conversation.id = :conversationId AND m.senderType != :readerType AND m.isRead = false")
    int markMessagesAsRead(
            @Param("conversationId") Long conversationId,
            @Param("readerType") String readerType,
            @Param("now") LocalDateTime now);
}
