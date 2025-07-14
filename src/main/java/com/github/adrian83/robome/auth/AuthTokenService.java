package com.github.adrian83.robome.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import static java.util.concurrent.CompletableFuture.failedFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.RefreshTokenException;
import com.github.adrian83.robome.auth.exception.TokenNotFoundException;
import com.github.adrian83.robome.auth.model.RefreshToken;
import com.github.adrian83.robome.auth.model.TokenResponse;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.user.model.Role;
import com.google.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class AuthTokenService {

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROLES = "roles";

    private static final String SECURITY_SECRET = "fa23hgrb23rv2394g0x81hyr275tcgr23gxc2435g43og527x";
    private static final Algorithm SECURITY_ALGORITHM = Algorithm.HMAC256(SECURITY_SECRET);

    private static final Duration TOKEN_EXPIRE_IN_HOURS = Duration.ofHours(2);
    private static final String TOKEN_ISSUER = "robome";

    private static final JWTVerifier VERIFIER = JWT.require(SECURITY_ALGORITHM)
            .withIssuer(TOKEN_ISSUER)
            .build();



    private static final Duration REFRESH_TOKEN_EXPIRE_IN = Duration.ofDays(30);

    private final RefreshTokenRepository repository;
    private final Authentication authentication;
    private final ActorSystem actorSystem;

    @Inject
    public AuthTokenService(RefreshTokenRepository repository,
            Authentication authentication, ActorSystem actorSystem) {
        this.repository = repository;
        this.authentication = authentication;
        this.actorSystem = actorSystem;
    }

    public CompletionStage<TokenResponse> createTokens(UserData user) {
        String accessToken = createAuthToken(user);
        long accessTokenExpiresIn = getTokenExpirationInSeconds();

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
                    String accessToken = createAuthToken(user);
                    long accessTokenExpiresIn;
            accessTokenExpiresIn = getTokenExpirationInSeconds();

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



    public long getTokenExpirationInSeconds() {
        return TOKEN_EXPIRE_IN_HOURS.getSeconds();
    }

    public String createAuthToken(UserData user) {
        try {
            return JWT.create()
                    .withSubject(user.email())
                    .withClaim(CLAIM_ID, user.id().toString())
                    .withArrayClaim(CLAIM_ROLES, user.roleNames())
                    .withIssuer(TOKEN_ISSUER)
                    .withExpiresAt(expirationDate())
                    .sign(SECURITY_ALGORITHM);

        } catch (JWTCreationException ex) {
            throw new InvalidSignInDataException("cannot create jwt token from user data", ex);
        }
    }

    public UserData extractUserDataFromToken(String token) {
        try {
            DecodedJWT jwt = VERIFIER.verify(token);
            return new UserData(extractUserId(jwt), jwt.getSubject(), extractRoles(jwt));
        } catch (JWTVerificationException ex) {
            throw new TokenNotFoundException("cannot verify jwt token", ex);
        }
    }

    private Set<Role> extractRoles(DecodedJWT jwt) {
        return jwt.getClaim(CLAIM_ROLES)
                .asList(String.class)
                .stream()
                .map(roleStr -> Role.valueOf(roleStr))
                .collect(Collectors.toSet());
    }

    private UUID extractUserId(DecodedJWT jwt) {
        return UUID.fromString(jwt.getClaim(CLAIM_ID).asString());
    }

    private Date expirationDate() {
        return Date.from(Instant.now().plusSeconds(TOKEN_EXPIRE_IN_HOURS.getSeconds()));
    }



}
