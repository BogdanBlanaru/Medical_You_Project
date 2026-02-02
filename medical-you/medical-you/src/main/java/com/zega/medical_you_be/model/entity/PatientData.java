package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_data")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PatientData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double height;

    @ManyToOne
    @JoinColumn(name = "chat_log_id", referencedColumnName = "id", nullable = false)
    private ChatLog chatLog;

    @Column
    private String[] symptoms;

    @Column(name = "created_at")
    @LastModifiedDate
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
