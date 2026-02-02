package com.zega.medical_you_be.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "family_groups")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FamilyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Patient createdBy;

    @OneToMany(mappedBy = "familyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<FamilyMember> members = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to add a member
    public void addMember(FamilyMember member) {
        members.add(member);
        member.setFamilyGroup(this);
    }

    // Helper method to remove a member
    public void removeMember(FamilyMember member) {
        members.remove(member);
        member.setFamilyGroup(null);
    }
}
