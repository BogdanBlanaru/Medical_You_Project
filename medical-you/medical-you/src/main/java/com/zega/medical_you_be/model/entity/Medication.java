package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.MedicationFrequency;
import com.zega.medical_you_be.model.enums.MedicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medications")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id")
    private FamilyMember familyMember;

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MedicationFrequency frequency;

    @Column(name = "times_per_day")
    @Builder.Default
    private Integer timesPerDay = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specific_times", columnDefinition = "jsonb")
    private List<String> specificTimes;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "prescribed_by")
    private String prescribedBy;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MedicationStatus status = MedicationStatus.ACTIVE;

    @Column(name = "refill_reminder_days")
    @Builder.Default
    private Integer refillReminderDays = 7;

    @Column(name = "pills_remaining")
    private Integer pillsRemaining;

    @Column(name = "pills_per_dose")
    @Builder.Default
    private Integer pillsPerDose = 1;

    @Column(length = 20)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicationLog> logs = new ArrayList<>();

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicationReminder> reminders = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to check if medication is currently active
    public boolean isCurrentlyActive() {
        if (status != MedicationStatus.ACTIVE) return false;
        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today)) return false;
        return endDate == null || !endDate.isBefore(today);
    }

    // Helper method to check if refill is needed
    public boolean needsRefill() {
        if (pillsRemaining == null || pillsPerDose == null) return false;
        int daysOfMedsRemaining = pillsRemaining / (pillsPerDose * timesPerDay);
        return refillReminderDays != null && daysOfMedsRemaining <= refillReminderDays;
    }

    // Helper method to decrease pills after taking medication
    public void decreasePills() {
        if (pillsRemaining != null && pillsPerDose != null) {
            pillsRemaining = Math.max(0, pillsRemaining - pillsPerDose);
        }
    }
}
