package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.entity.Doctor;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.entity.Token;
import com.zega.medical_you_be.repo.TokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepo tokenRepo;

    @Override
    public Token saveDoctorToken(Doctor doctor, String token) {
        return tokenRepo.save(
                Token.builder()
                        .doctor(doctor)
                        .token(token)
                        .expired(false)
                        .revoked(false)
                        .build()
        );
    }

    @Override
    public Token savePatientToken(Patient patient, String token) {
        return tokenRepo.save(
                Token.builder()
                        .patient(patient)
                        .token(token)
                        .expired(false)
                        .revoked(false)
                        .build()
        );
    }

    @Override
    public boolean revokeToken(String token) {
        Optional<Token> optionalToken = tokenRepo.findByTokenNotExpiredAndNotRevoked(token);
        if (optionalToken.isPresent()) {
            Token jwtToken = optionalToken.get();
            jwtToken.setExpired(true);
            jwtToken.setRevoked(true);
            tokenRepo.save(jwtToken);
            return true;
        }
        return false;
    }

    @Override
    public void revokeAllTokens(Long id) {
        tokenRepo.findTokensByUserId(id)
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                    tokenRepo.save(token);
                });
    }
}
