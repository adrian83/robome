package com.github.adrian83.robome.auth;

import java.security.Key;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.crypto.spec.SecretKeySpec;

import com.github.adrian83.robome.domain.user.User;
import com.github.adrian83.robome.domain.user.UserService;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class JwtAuthorizer extends AllDirectives {

	private static final String BEARER = "Bearer ";

	private static final String SECURITY_KEY = "security.key";
	
	private static final String USER_EMAIL = "user_email";
	private static final String USER_ID = "user_id ";

	private static final SignatureAlgorithm SECURITY_ALGORITHM = SignatureAlgorithm.HS512;

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
				.setClaims(ImmutableMap.of(USER_EMAIL, user.getEmail(),USER_ID, user.getId().toString()))
				.signWith(SECURITY_ALGORITHM, getSecurityKey())
				.compact();
	}
	
	public String createAuthorizationToken(User user) {
		return BEARER + createJWTToken(user);
	}

	public Key getSecurityKey() {
		return new SecretKeySpec(config.getString(SECURITY_KEY).getBytes(), SECURITY_ALGORITHM.getValue());
	}

	public Route authorized(Optional<String> maybeJwtToken, Supplier<Route> inner) {
		return procedeIfValidToken(maybeJwtToken, userData -> inner.get());
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

				AuthContext authContext = fromJwsToken(jwtToken.replaceFirst(BEARER, ""));
				CompletionStage<Optional<User>> maybeUserF = userService.findUserByEmail(authContext.getUserEmail());
				return inner.apply(maybeUserF);

			} catch (SignatureException | MalformedJwtException e) {
				return complete(HttpResponse.create().withStatus(StatusCodes.UNAUTHORIZED));

			} catch (Exception e) {
				return complete(
						HttpResponse.create().withStatus(StatusCodes.INTERNAL_SERVER_ERROR).withEntity(e.getMessage()));
			}
		}).orElse(complete(HttpResponse.create().withStatus(StatusCodes.UNAUTHORIZED)));
	}

	private AuthContext fromJwsToken(String jwtToken) {
		Key key = getSecurityKey();

		Jws<Claims> jwt = Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken);

		Claims body = jwt.getBody();

		String email = body.get(USER_EMAIL).toString();
		UUID userId = UUID.fromString(body.get(USER_ID).toString());

		return new AuthContext(userId, email, jwtToken);
	}

}
