package com.github.adrian83.robome.auth.model;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
    UUID tokenId, 
    UUID userId, 
    String refreshToken,
    UUID tokenFamily,
    Instant issuedAt,
    Instant expiresAt) {
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
