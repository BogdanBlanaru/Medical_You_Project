package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.AlertSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_alerts")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HealthAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_reading_id", nullable = false)
    private HealthReading healthReading;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "is_acknowledged", nullable = false)
    @Builder.Default
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper method to acknowledge this alert
    public void acknowledge() {
        this.isAcknowledged = true;
        this.acknowledgedAt = LocalDateTime.now();
    }
}
