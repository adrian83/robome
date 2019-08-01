package com.github.adrian83.robome.auth;

import java.security.Key;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.user.UserService;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class JwtAuthorizer extends AllDirectives {

	private static final SignatureAlgorithm SECURITY_ALGORITHM = SignatureAlgorithm.HS512;
	
	private static final String BEARER = "Bearer ";
	private static final String SECURITY_KEY = "security.key";
	private static final String USER_EMAIL = "user_email";
	

	private Config config;
	private UserService userService;
	private Response responseProducer;

	@Inject
	public JwtAuthorizer(Config config, UserService userService, Response responseProducer) {
		this.config = config;
		this.userService = userService;
		this.responseProducer = responseProducer;
	}

	public String createJWTToken(User user) {
		return Jwts.builder()
				.setSubject("UserData")
				.setClaims(ImmutableMap.of(USER_EMAIL, user.getEmail()))
				.signWith(SECURITY_ALGORITHM, getSecurityKey())
				.compact();
	}
	
	public String createAuthorizationToken(User user) {
		return BEARER + createJWTToken(user);
	}

	public Key getSecurityKey() {
		return new SecretKeySpec(config.getString(SECURITY_KEY).getBytes(), SECURITY_ALGORITHM.getValue());
	}

	public Route authorized(Optional<String> maybeJwtToken, Function<CompletionStage<Optional<User>>, Route> inner) {
		return procedeIfValidToken(maybeJwtToken, inner::apply);
	}

	public Route procedeIfValidToken(Optional<String> maybeJwtToken, Function<CompletionStage<Optional<User>>, Route> inner) {
		return maybeJwtToken.map(jwtToken -> {
			try {

				if (!jwtToken.startsWith(BEARER)) {
					throw new MalformedJwtException("Invalid JWT token");
				}

				var email = emailFromJwsToken(jwtToken.replaceFirst(BEARER, ""));
				var maybeUserF = userService.findUserByEmail(email);
				return inner.apply(maybeUserF);

			} catch (SignatureException | MalformedJwtException e) {
				return complete(responseProducer.response401());

			} catch (Exception e) {
				return complete(responseProducer.response500(e.getMessage()));
			}
		}).orElse(complete(responseProducer.response401()));
	}

	private String emailFromJwsToken(String jwtToken) {
		Key key = getSecurityKey();
		Jws<Claims> jwt = Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken);
		Claims body = jwt.getBody();
		return body.get(USER_EMAIL).toString();
	}

}
