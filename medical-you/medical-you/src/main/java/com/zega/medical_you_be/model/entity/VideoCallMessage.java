package com.zega.medical_you_be.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity for storing video call chat messages.
 * Messages are grouped by room ID (call session identifier).
 */
@Entity
@Table(name = "video_call_messages", indexes = {
    @Index(name = "idx_vcm_room_id", columnList = "room_id"),
    @Index(name = "idx_vcm_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VideoCallMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_type")
    private String senderType; // "PATIENT" or "DOCTOR"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
