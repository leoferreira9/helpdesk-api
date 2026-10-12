package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.entity.EmailVerificationToken;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.repository.EmailVerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class EmailVerificationTokenService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public EmailVerificationTokenService(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private String hashToken(String token) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }

        byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(bytes);
    }

    public String createToken(User user) {
        String token = generateRandomUUID();
        String hash = hashToken(token);

        EmailVerificationToken emailVerificationToken = new EmailVerificationToken();
        emailVerificationToken.setUser(user);
        emailVerificationToken.setTokenHash(hash);
        emailVerificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        emailVerificationTokenRepository.save(emailVerificationToken);
        return token;
    }
}
