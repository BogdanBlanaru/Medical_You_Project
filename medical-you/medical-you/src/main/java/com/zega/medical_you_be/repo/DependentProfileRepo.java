package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.DependentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DependentProfileRepo extends JpaRepository<DependentProfile, Long> {

    /**
     * Find profile by family member ID
     */
    Optional<DependentProfile> findByFamilyMemberId(Long familyMemberId);

    /**
     * Find profile by medical ID
     */
    Optional<DependentProfile> findByMedicalId(String medicalId);

    /**
     * Find profile with family member eagerly loaded
     */
    @Query("SELECT dp FROM DependentProfile dp JOIN FETCH dp.familyMember WHERE dp.id = :profileId")
    Optional<DependentProfile> findByIdWithFamilyMember(@Param("profileId") Long profileId);

    /**
     * Check if a medical ID already exists
     */
    boolean existsByMedicalId(String medicalId);
}
