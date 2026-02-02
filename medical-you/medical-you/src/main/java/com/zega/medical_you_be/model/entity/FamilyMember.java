package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "family_members")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id", nullable = false)
    private FamilyGroup familyGroup;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    private RelationshipType relationshipType;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToOne(mappedBy = "familyMember", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DependentProfile dependentProfile;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to calculate age
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // Helper method to check if this is the account owner
    public boolean isSelf() {
        return RelationshipType.SELF.equals(this.relationshipType);
    }

    // Helper method to set dependent profile
    public void setDependentProfile(DependentProfile profile) {
        this.dependentProfile = profile;
        if (profile != null) {
            profile.setFamilyMember(this);
        }
    }
}
