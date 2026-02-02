package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.ReadingType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "health_readings")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HealthReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id")
    private FamilyMember familyMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false, length = 30)
    private ReadingType readingType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "secondary_value", precision = 10, scale = 2)
    private BigDecimal secondaryValue;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "healthReading", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HealthAlert> alerts = new ArrayList<>();

    // Helper method to get display value based on reading type
    public String getDisplayValue() {
        if (readingType == ReadingType.BLOOD_PRESSURE && secondaryValue != null) {
            return value.intValue() + "/" + secondaryValue.intValue() + " " + unit;
        }
        return value.toString() + " " + unit;
    }

    // Helper method to check if this reading has any unacknowledged alerts
    public boolean hasUnacknowledgedAlerts() {
        return alerts.stream().anyMatch(alert -> !alert.getIsAcknowledged());
    }
}
