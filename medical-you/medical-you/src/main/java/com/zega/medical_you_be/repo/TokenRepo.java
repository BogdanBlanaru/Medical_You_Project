package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepo extends JpaRepository<Token, Long> {
    // This query checks both the patient and doctor for tokens
    @Query("select t from Token t " +
            "left join t.patient p " +
            "left join t.doctor d " +
            "where (p.id = :id or d.id = :id) " +
            "and t.expired = false and t.revoked = false")
    List<Token> findTokensByUserId(@Param("id") Long id);

    // This query checks for the token if it's not expired and not revoked
    @Query("select t from Token t where t.token = :token " +
            "and t.revoked = false and t.expired = false")
    Optional<Token> findByTokenNotExpiredAndNotRevoked(@Param("token") String token);
}
