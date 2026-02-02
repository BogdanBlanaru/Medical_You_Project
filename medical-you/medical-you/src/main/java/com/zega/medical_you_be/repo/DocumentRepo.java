package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.Document;
import com.zega.medical_you_be.model.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {

    // Basic queries
    Page<Document> findByPatientIdOrderByUploadedAtDesc(Long patientId, Pageable pageable);

    Optional<Document> findByIdAndPatientId(Long id, Long patientId);

    // Filter by folder
    Page<Document> findByPatientIdAndFolderIdOrderByUploadedAtDesc(Long patientId, Long folderId, Pageable pageable);

    Page<Document> findByPatientIdAndFolderIsNullOrderByUploadedAtDesc(Long patientId, Pageable pageable);

    // Filter by type
    Page<Document> findByPatientIdAndDocumentTypeOrderByUploadedAtDesc(Long patientId, DocumentType type, Pageable pageable);

    // Filter by family member
    Page<Document> findByPatientIdAndFamilyMemberIdOrderByUploadedAtDesc(Long patientId, Long familyMemberId, Pageable pageable);

    // Combined filters
    @Query("SELECT d FROM Document d WHERE d.patient.id = :patientId " +
           "AND (:folderId IS NULL OR d.folder.id = :folderId) " +
           "AND (:documentType IS NULL OR d.documentType = :documentType) " +
           "AND (:familyMemberId IS NULL OR d.familyMember.id = :familyMemberId) " +
           "ORDER BY d.uploadedAt DESC")
    Page<Document> findWithFilters(
            @Param("patientId") Long patientId,
            @Param("folderId") Long folderId,
            @Param("documentType") DocumentType documentType,
            @Param("familyMemberId") Long familyMemberId,
            Pageable pageable);

    // Search by title or description
    @Query("SELECT d FROM Document d WHERE d.patient.id = :patientId " +
           "AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY d.uploadedAt DESC")
    Page<Document> searchDocuments(@Param("patientId") Long patientId, @Param("query") String query, Pageable pageable);

    // Shared documents
    List<Document> findByPatientIdAndIsSharedWithDoctorTrueOrderByUploadedAtDesc(Long patientId);

    // Statistics
    @Query("SELECT COUNT(d) FROM Document d WHERE d.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.patient.id = :patientId")
    Long sumFileSizeByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT d.documentType, COUNT(d) FROM Document d WHERE d.patient.id = :patientId GROUP BY d.documentType")
    List<Object[]> countByDocumentType(@Param("patientId") Long patientId);

    // Recent documents
    List<Document> findTop10ByPatientIdOrderByUploadedAtDesc(Long patientId);

    // Documents for doctor
    @Query("SELECT d FROM Document d WHERE d.patient.id = :patientId AND d.isSharedWithDoctor = true ORDER BY d.uploadedAt DESC")
    List<Document> findSharedWithDoctor(@Param("patientId") Long patientId);
}
