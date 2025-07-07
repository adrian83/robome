package com.github.adrian83.robome.auth.model;

public record TokenResponse(
    String accessToken, 
    String refreshToken,
    long expiresIn) {
    
    public static TokenResponse of(String accessToken, String refreshToken, long expiresInSeconds) {
        return new TokenResponse(accessToken, refreshToken, expiresInSeconds);
    }
}
