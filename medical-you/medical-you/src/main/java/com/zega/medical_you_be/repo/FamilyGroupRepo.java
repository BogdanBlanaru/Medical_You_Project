package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.FamilyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyGroupRepo extends JpaRepository<FamilyGroup, Long> {

    /**
     * Find family group created by a specific patient
     */
    Optional<FamilyGroup> findByCreatedById(Long patientId);

    /**
     * Check if a patient has already created a family group
     */
    boolean existsByCreatedById(Long patientId);

    /**
     * Find family group with all members eagerly loaded
     */
    @Query("SELECT fg FROM FamilyGroup fg LEFT JOIN FETCH fg.members WHERE fg.id = :groupId")
    Optional<FamilyGroup> findByIdWithMembers(@Param("groupId") Long groupId);

    /**
     * Find family group by patient ID with members
     */
    @Query("SELECT fg FROM FamilyGroup fg LEFT JOIN FETCH fg.members WHERE fg.createdBy.id = :patientId")
    Optional<FamilyGroup> findByCreatedByIdWithMembers(@Param("patientId") Long patientId);
}
