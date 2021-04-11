package com.github.adrian83.robome.auth;

import java.util.Date;
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

  private static final String SECRET = "fa23hgrb23rv2394g0x81hyr275tcgr23gxc2435g43og527x";
  private static final Algorithm SECURITY_ALGORITHM = Algorithm.HMAC256(SECRET);

  private static final Long EXPIRE_IN_MILLIS = 1000L * 60 * 60 * 2; // 2h

  private static final String ISSUER = "robome";

  private static final JWTVerifier VERIFIER =
      JWT.require(SECURITY_ALGORITHM).withIssuer(ISSUER).build();

  @Inject
  public JwtAuthorizer() {}

  public String createToken(UserData user) {
    try {
      return JWT.create()
          .withSubject(user.getEmail())
          .withClaim("id", user.getId().toString())
          .withArrayClaim(
              "roles", user.getRoles().stream().map(r -> r.name()).toArray(String[]::new))
          .withIssuer(ISSUER)
          .withExpiresAt(expirationDate())
          .sign(SECURITY_ALGORITHM);

    } catch (JWTCreationException ex) {
      throw new InvalidSignInDataException("cannot create jwt token from user data", ex);
    }
  }

  public UserData userFromToken(String token) {
    try {
      DecodedJWT jwt = VERIFIER.verify(token);
      var email = jwt.getSubject();
      var id = UUID.fromString(jwt.getClaim("id").asString());
      var roles =
          jwt.getClaim("roles")
              .asList(String.class)
              .stream()
              .map(roleStr -> Role.valueOf(roleStr))
              .collect(Collectors.toSet());

      return UserData.builder().id(id).email(email).roles(roles).build();
    } catch (JWTVerificationException ex) {
      throw new TokenNotFoundException("cannot verify jwt token", ex);
    }
  }

  private Date expirationDate() {
    return new Date(System.currentTimeMillis() + EXPIRE_IN_MILLIS);
  }
}
