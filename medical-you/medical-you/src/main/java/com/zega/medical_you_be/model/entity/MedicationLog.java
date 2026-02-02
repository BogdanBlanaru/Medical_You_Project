package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.MedicationLogStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_logs")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MedicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicationLogStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper method to check if taken on time (within 30 minutes of scheduled time)
    public boolean isTakenOnTime() {
        if (scheduledTime == null || takenAt == null) return true;
        long minutesDiff = java.time.Duration.between(scheduledTime, takenAt).toMinutes();
        return Math.abs(minutesDiff) <= 30;
    }
}
