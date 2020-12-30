package com.github.adrian83.robome.auth;

import java.security.Key;
import java.util.Optional;

import javax.crypto.spec.SecretKeySpec;

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

  private Key securityKey;

  @Inject
  public JwtAuthorizer(Config config) {
    this.securityKey =
        new SecretKeySpec(config.getString(SECURITY_KEY).getBytes(), SECURITY_ALGORITHM.getValue());
  }

  public String createToken(User user) {
    return Jwts.builder()
        .setSubject("UserData")
        .setClaims(ImmutableMap.of(USER_EMAIL, user.getEmail()))
        .signWith(SECURITY_ALGORITHM, securityKey)
        .compact();
  }

  public Optional<String> emailFromToken(String token) {
    Jws<Claims> jwt = Jwts.parser().setSigningKey(securityKey).parseClaimsJws(token);
    Claims body = jwt.getBody();
    return Optional.ofNullable(body)
        .map(c -> c.get(USER_EMAIL))
        .map((emailObj) -> emailObj.toString());
  }
}
