
package com.ecommerce.project.service;

import com.ecommerce.project.email.EmailService;
import com.ecommerce.project.model.EmailVerificationToken;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.EmailVerificationTokenRepository;

import com.ecommerce.project.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;

import com.ecommerce.project.service.RateLimitService;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailService emailService;

    private final RateLimitService rateLimitService;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository, UserRepository userRepository, EmailService emailService, RateLimitService rateLimitService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public String generateVerificationToken(User user) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken =
                tokenRepository.findByUserUserId(user.getUserId())
                        .orElseGet(EmailVerificationToken::new);

        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setExpiresAt(
                LocalDateTime.now(ZoneOffset.UTC).plusHours(24)
        );

        tokenRepository.save(verificationToken);

        return rawToken;
    }


    @Transactional
    public void verifyEmail(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Verifikacijski token je potreban"
            );
        }

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nevažeći token za verifikaciju"
                                )
                        );

        if (!verificationToken.getExpiresAt()
                .isAfter(LocalDateTime.now(ZoneOffset.UTC))) {

            throw new IllegalArgumentException(
                    "Verifikacijski token je istekao"
            );
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        tokenRepository.delete(verificationToken);
    }


    @Transactional
    public void resendVerificationEmail(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim())
                .ifPresent(user -> {

                    if (user.isEmailVerified()) {
                        return;
                    }

                    // Provjera vremenskog ograničenja
                    var existingToken = tokenRepository
                            .findByUserUserId(user.getUserId());

                    if (existingToken.isPresent()) {

                        LocalDateTime expiresAt =
                                existingToken.get().getExpiresAt();

                        // Token vrijedi 24 sata.
                        // Ako je kreiran prije manje od 60 sekundi,
                        // ne šaljemo novi email.
                        LocalDateTime createdAt =
                                expiresAt.minusHours(24);

                        if (createdAt.isAfter(
                                LocalDateTime.now(ZoneOffset.UTC)
                                        .minusSeconds(60))) {
                            return;
                        }
                    }

                    String token =
                            generateVerificationToken(user);

                    emailService.sendVerificationEmail(
                            user.getEmail(),
                            token
                    );
                });
    }


    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable", e
            );
        }
    }
}
