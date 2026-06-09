package com.example.chat_app.services;

import com.example.chat_app.entities.RefreshTokenEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration}")
    private long REFRESH_EXPIRATION_TIME;

    @Transactional
    public RefreshTokenEntity create(UserEntity user, String token) {
        // Revoke all existing tokens for user (single session)
        refreshTokenRepository.revokeAllByUser(user);

        RefreshTokenEntity rt = new RefreshTokenEntity();
        rt.setToken(token);
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plusMillis(REFRESH_EXPIRATION_TIME));
        return refreshTokenRepository.save(rt);
    }

    public RefreshTokenEntity validate(String token) {
        RefreshTokenEntity rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (rt.isRevoked()) throw new RuntimeException("Refresh token revoked");
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            throw new RuntimeException("Refresh token expired");
        }
        return rt;
    }

    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}