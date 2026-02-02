package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.DocumentFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentFolderRepo extends JpaRepository<DocumentFolder, Long> {

    List<DocumentFolder> findByPatientIdOrderByNameAsc(Long patientId);

    List<DocumentFolder> findByPatientIdAndParentFolderIsNullOrderByNameAsc(Long patientId);

    List<DocumentFolder> findByPatientIdAndParentFolderIdOrderByNameAsc(Long patientId, Long parentFolderId);

    Optional<DocumentFolder> findByIdAndPatientId(Long id, Long patientId);

    boolean existsByPatientIdAndNameAndParentFolderId(Long patientId, String name, Long parentFolderId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.folder.id = :folderId")
    int countDocumentsByFolderId(@Param("folderId") Long folderId);

    @Query("SELECT f FROM DocumentFolder f LEFT JOIN FETCH f.subFolders WHERE f.patient.id = :patientId AND f.parentFolder IS NULL ORDER BY f.name")
    List<DocumentFolder> findRootFoldersWithSubFolders(@Param("patientId") Long patientId);
}
