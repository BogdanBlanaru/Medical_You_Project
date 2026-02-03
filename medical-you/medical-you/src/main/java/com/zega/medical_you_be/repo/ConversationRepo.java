package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.Conversation;
import com.zega.medical_you_be.model.enums.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.patient.id = :patientId " +
           "AND (:status IS NULL OR c.status = :status) " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findByPatientIdAndStatus(
            @Param("patientId") Long patientId,
            @Param("status") ConversationStatus status,
            Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE c.doctor.id = :doctorId " +
           "AND (:status IS NULL OR c.status = :status) " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findByDoctorIdAndStatus(
            @Param("doctorId") Long doctorId,
            @Param("status") ConversationStatus status,
            Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE c.id = :id AND c.patient.id = :patientId")
    Optional<Conversation> findByIdAndPatientId(@Param("id") Long id, @Param("patientId") Long patientId);

    @Query("SELECT c FROM Conversation c WHERE c.id = :id AND c.doctor.id = :doctorId")
    Optional<Conversation> findByIdAndDoctorId(@Param("id") Long id, @Param("doctorId") Long doctorId);

    @Query("SELECT COALESCE(SUM(c.unreadCountPatient), 0) FROM Conversation c WHERE c.patient.id = :patientId")
    Integer countTotalUnreadForPatient(@Param("patientId") Long patientId);

    @Query("SELECT COALESCE(SUM(c.unreadCountDoctor), 0) FROM Conversation c WHERE c.doctor.id = :doctorId")
    Integer countTotalUnreadForDoctor(@Param("doctorId") Long doctorId);

    @Modifying
    @Query("UPDATE Conversation c SET c.unreadCountPatient = 0 WHERE c.id = :conversationId")
    void resetPatientUnreadCount(@Param("conversationId") Long conversationId);

    @Modifying
    @Query("UPDATE Conversation c SET c.unreadCountDoctor = 0 WHERE c.id = :conversationId")
    void resetDoctorUnreadCount(@Param("conversationId") Long conversationId);
}
