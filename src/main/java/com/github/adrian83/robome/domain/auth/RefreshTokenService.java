package com.github.adrian83.robome.domain.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import static java.util.concurrent.CompletableFuture.failedFuture;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.Authentication;
import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.auth.exception.RefreshTokenException;
import com.github.adrian83.robome.auth.model.RefreshToken;
import com.github.adrian83.robome.auth.model.TokenResponse;
import com.github.adrian83.robome.auth.model.UserData;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_EXPIRE_IN = Duration.ofDays(30);

    private final RefreshTokenRepository repository;
    private final JwtAuthorizer jwtAuthorizer;
    private final Authentication authentication;
    private final ActorSystem actorSystem;

    @Inject
    public RefreshTokenService(RefreshTokenRepository repository, JwtAuthorizer jwtAuthorizer, 
            Authentication authentication, ActorSystem actorSystem) {
        this.repository = repository;
        this.jwtAuthorizer = jwtAuthorizer;
        this.authentication = authentication;
        this.actorSystem = actorSystem;
    }

    public CompletionStage<TokenResponse> createTokens(UserData user) {
        String accessToken = jwtAuthorizer.createToken(user);
        long accessTokenExpiresIn = jwtAuthorizer.getTokenExpirationInSeconds();

        RefreshToken refreshToken = createRefreshToken(user.id());
        return Source.single(refreshToken)
                .via(repository.saveToken())
                .map(saved -> TokenResponse.of(accessToken, saved.refreshToken(), accessTokenExpiresIn))
                .runWith(Sink.head(), actorSystem);
    }

    public CompletionStage<TokenResponse> refreshTokens(String refreshTokenStr) {
        return repository.findByToken(refreshTokenStr)
                .runWith(Sink.headOption(), actorSystem)
                .thenCompose(maybeToken -> maybeToken
                        .map(this::handleValidToken)
                        .orElseThrow(() -> new RefreshTokenException("Invalid refresh token")));
    }

    public CompletionStage<Void> revokeUserTokens(UUID userId) {
        return Source.single(userId)
                .via(repository.deleteByUserId())
                .runWith(Sink.ignore(), actorSystem)
                .thenApply(done -> null);
    }

    private CompletionStage<TokenResponse> handleValidToken(RefreshToken token) {
        if (token.isExpired()) {
            return revokeTokenFamily(token.tokenFamily())
                    .thenCompose(v -> failedFuture(new RefreshTokenException("Refresh token expired")));
        }

        return authentication.findUserById(token.userId())
                .thenCompose(user -> {
                    String accessToken = jwtAuthorizer.createToken(user);
                    long accessTokenExpiresIn = jwtAuthorizer.getTokenExpirationInSeconds();

                    // Rotate refresh token
                    RefreshToken newToken = createRefreshToken(user.id(), token.tokenFamily());
                    return Source.single(newToken)
                            .via(repository.saveToken())
                            .map(saved -> TokenResponse.of(accessToken, saved.refreshToken(), accessTokenExpiresIn))
                            .runWith(Sink.head(), actorSystem);
                });
    }

    private CompletionStage<Void> revokeTokenFamily(UUID family) {
        return Source.single(family)
                .via(repository.deleteByFamily())
                .runWith(Sink.ignore(), actorSystem)
                .thenApply(done -> null);
    }

    private RefreshToken createRefreshToken(UUID userId) {
        return createRefreshToken(userId, UUID.randomUUID());
    }

    private RefreshToken createRefreshToken(UUID userId, UUID family) {
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID().toString(),
                family,
                Instant.now(),
                Instant.now().plus(REFRESH_TOKEN_EXPIRE_IN));
    }
}
