package com.leonardo.helpdesk.repository;

import com.leonardo.helpdesk.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
}
