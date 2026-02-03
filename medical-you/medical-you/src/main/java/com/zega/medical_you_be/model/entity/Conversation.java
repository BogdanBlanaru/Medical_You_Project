package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conv_patient_id", columnList = "patient_id"),
    @Index(name = "idx_conv_doctor_id", columnList = "doctor_id"),
    @Index(name = "idx_conv_family_member_id", columnList = "family_member_id"),
    @Index(name = "idx_conv_status", columnList = "status"),
    @Index(name = "idx_conv_last_message_at", columnList = "last_message_at")
})
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id")
    private FamilyMember familyMember;

    @Column(nullable = false, length = 200)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Column(name = "unread_count_patient")
    @Builder.Default
    private Integer unreadCountPatient = 0;

    @Column(name = "unread_count_doctor")
    @Builder.Default
    private Integer unreadCountDoctor = 0;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void incrementUnreadCount(String senderType) {
        if ("PATIENT".equals(senderType)) {
            this.unreadCountDoctor = (this.unreadCountDoctor == null ? 0 : this.unreadCountDoctor) + 1;
        } else {
            this.unreadCountPatient = (this.unreadCountPatient == null ? 0 : this.unreadCountPatient) + 1;
        }
    }

    public void resetUnreadCount(String readerType) {
        if ("PATIENT".equals(readerType)) {
            this.unreadCountPatient = 0;
        } else {
            this.unreadCountDoctor = 0;
        }
    }
}
