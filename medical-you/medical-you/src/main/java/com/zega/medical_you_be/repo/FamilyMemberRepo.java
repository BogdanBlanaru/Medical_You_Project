package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.FamilyMember;
import com.zega.medical_you_be.model.enums.RelationshipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepo extends JpaRepository<FamilyMember, Long> {

    /**
     * Find all active members of a family group
     */
    List<FamilyMember> findByFamilyGroupIdAndIsActiveTrue(Long familyGroupId);

    /**
     * Find all members of a family group (including inactive)
     */
    List<FamilyMember> findByFamilyGroupId(Long familyGroupId);

    /**
     * Find the SELF member of a family group (the account owner)
     */
    @Query("SELECT fm FROM FamilyMember fm WHERE fm.familyGroup.id = :familyGroupId AND fm.relationshipType = 'SELF'")
    Optional<FamilyMember> findSelfMember(@Param("familyGroupId") Long familyGroupId);

    /**
     * Find a family member with their dependent profile
     */
    @Query("SELECT fm FROM FamilyMember fm LEFT JOIN FETCH fm.dependentProfile WHERE fm.id = :memberId")
    Optional<FamilyMember> findByIdWithProfile(@Param("memberId") Long memberId);

    /**
     * Find family member by family group and relationship type
     */
    Optional<FamilyMember> findByFamilyGroupIdAndRelationshipType(Long familyGroupId, RelationshipType relationshipType);

    /**
     * Count active members in a family group
     */
    long countByFamilyGroupIdAndIsActiveTrue(Long familyGroupId);

    /**
     * Check if a family member belongs to a specific family group
     */
    @Query("SELECT CASE WHEN COUNT(fm) > 0 THEN true ELSE false END FROM FamilyMember fm " +
           "WHERE fm.id = :memberId AND fm.familyGroup.createdBy.id = :patientId")
    boolean belongsToPatient(@Param("memberId") Long memberId, @Param("patientId") Long patientId);

    /**
     * Find a family member by ID that belongs to a patient's family group
     */
    @Query("SELECT fm FROM FamilyMember fm WHERE fm.id = :memberId AND fm.familyGroup.createdBy.id = :patientId")
    Optional<FamilyMember> findByIdAndFamilyGroupPatientId(@Param("memberId") Long memberId, @Param("patientId") Long patientId);
}
