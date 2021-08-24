package com.github.adrian83.robome.auth;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.TokenNotFoundException;
import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.domain.user.model.Role;
import com.google.inject.Inject;

public class JwtAuthorizer {

  private static final String ID_CLAIM = "id";
  private static final String ROLES_CLAIM = "roles";

  private static final String SECURITY_SECRET = "fa23hgrb23rv2394g0x81hyr275tcgr23gxc2435g43og527x";
  private static final Algorithm SECURITY_ALGORITHM = Algorithm.HMAC256(SECURITY_SECRET);

  private static final Long TOKEN_EXPIRE_IN_MILLIS = 1000L * 60 * 60 * 2; // 2h
  private static final String TOKEN_ISSUER = "robome";

  private static final JWTVerifier VERIFIER =
      JWT.require(SECURITY_ALGORITHM).withIssuer(TOKEN_ISSUER).build();

  @Inject
  public JwtAuthorizer() {}

  public String createToken(UserData user) {
    try {
      return JWT.create()
          .withSubject(user.email())
          .withClaim(ID_CLAIM, user.id().toString())
          .withArrayClaim(ROLES_CLAIM, user.roleName())
          .withIssuer(TOKEN_ISSUER)
          .withExpiresAt(expirationDate())
          .sign(SECURITY_ALGORITHM);

    } catch (JWTCreationException ex) {
      throw new InvalidSignInDataException("cannot create jwt token from user data", ex);
    }
  }

  public UserData userFromToken(String token) {
    try {
      DecodedJWT jwt = VERIFIER.verify(token);
      return new UserData(extractUserId(jwt), jwt.getSubject(), extractRoles(jwt));
    } catch (JWTVerificationException ex) {
      throw new TokenNotFoundException("cannot verify jwt token", ex);
    }
  }

  private Set<Role> extractRoles(DecodedJWT jwt) {
    return jwt.getClaim(ROLES_CLAIM).asList(String.class).stream()
        .map(roleStr -> Role.valueOf(roleStr))
        .collect(Collectors.toSet());
  }

  private UUID extractUserId(DecodedJWT jwt) {
    return UUID.fromString(jwt.getClaim(ID_CLAIM).asString());
  }

  private Date expirationDate() {
    return new Date((Instant.now().getEpochSecond() * 1000) + TOKEN_EXPIRE_IN_MILLIS);
  }
}
