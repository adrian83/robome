package com.github.adrian83.robome.auth;

import static java.util.concurrent.CompletableFuture.completedStage;

import java.security.Key;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.crypto.spec.SecretKeySpec;

import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtAuthorizer {

  private static final SignatureAlgorithm SECURITY_ALGORITHM = SignatureAlgorithm.HS512;

  private static final String SECURITY_KEY = "security.key";
  private static final String USER_EMAIL = "user_email";

  private Config config;
  private UserService userService;

  @Inject
  public JwtAuthorizer(Config config, UserService userService) {
    this.config = config;
    this.userService = userService;
  }

  public String createJWTToken(User user) {
    return Jwts.builder()
        .setSubject("UserData")
        .setClaims(ImmutableMap.of(USER_EMAIL, user.getEmail()))
        .signWith(SECURITY_ALGORITHM, getSecurityKey())
        .compact();
  }

  public Key getSecurityKey() {
    return new SecretKeySpec(
        config.getString(SECURITY_KEY).getBytes(), SECURITY_ALGORITHM.getValue());
  }

  public CompletionStage<Optional<User>> findUser(Optional<String> maybeEmail) {
    return maybeEmail
        .map((email) -> userService.findUserByEmail(email))
        .orElse(completedStage(Optional.empty()));
  }

  public Optional<String> emailFromJwsToken(String jwtToken) {
    Key key = getSecurityKey();
    Jws<Claims> jwt = Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken);
    Claims body = jwt.getBody();
    return Optional.ofNullable(body.get(USER_EMAIL)).map((emailObj) -> emailObj.toString());
  }
}
