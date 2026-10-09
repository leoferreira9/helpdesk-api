package com.leonardo.helpdesk.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.leonardo.helpdesk.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private static final String ISSUER = "helpdesk-api";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expiration;

    public JwtService(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${spring.security.jwt.expiration}") long expiration
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(expiration))
                .sign(algorithm);
    }

    public boolean validateToken(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return verifier.verify(token).getSubject();
    }
}
