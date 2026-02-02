package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.VideoCallMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoCallMessageRepo extends JpaRepository<VideoCallMessage, Long> {

    @Query("SELECT m FROM VideoCallMessage m WHERE m.roomId = :roomId AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<VideoCallMessage> findByRoomId(@Param("roomId") String roomId);

    @Query("SELECT m FROM VideoCallMessage m WHERE m.roomId = :roomId AND m.createdAt > :since AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<VideoCallMessage> findByRoomIdSince(@Param("roomId") String roomId, @Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT m.roomId FROM VideoCallMessage m WHERE m.senderId = :userId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<String> findRoomIdsByUserId(@Param("userId") Long userId);
}
