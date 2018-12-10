package ab.java.robome.web.security;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.crypto.spec.SecretKeySpec;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import ab.java.robome.domain.user.model.User;
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

public class SecurityUtils extends AllDirectives {

	private static final String BEARER = "Bearer ";

	private static final String USER_EMAIL = "user_email ";
	private static final String USER_ID = "user_id ";

	private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS512;

	private Config config;

	@Inject
	public SecurityUtils(Config config) {
		this.config = config;
	}

	public String createJWTToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(USER_EMAIL, user.getEmail());
		claims.put(USER_ID, user.getId().toString());

		String compactJws = Jwts.builder().setSubject("UserData").setClaims(claims)
				.signWith(ALGORITHM, getSecurityKey()).compact();
		return compactJws;
	}

	public String createAuthorizationToken(User user) {
		return BEARER + createJWTToken(user);
	}

	public Key getSecurityKey() {
		return new SecretKeySpec(config.getString("security.key").getBytes(), ALGORITHM.getValue());
	}

	public Route authorized(Optional<String> maybeJwtToken, Supplier<Route> inner) {
		return procedeIfValidToken(maybeJwtToken, userData -> inner.get());
	}

	public Route authorized(Optional<String> maybeJwtToken, Function<UserData, Route> inner) {
		return procedeIfValidToken(maybeJwtToken, inner::apply);
	}

	public Route procedeIfValidToken(Optional<String> maybeJwtToken, Function<UserData, Route> inner) {
		return maybeJwtToken.map(jwtToken -> {
			try {

				if (!jwtToken.startsWith(BEARER)) {
					throw new MalformedJwtException("Invalid JWT token");
				}

				UserData userData = fromJwsToken(jwtToken.replaceFirst(BEARER, ""));
				return inner.apply(userData);

			} catch (SignatureException | MalformedJwtException e) {
				return complete(HttpResponse.create().withStatus(StatusCodes.UNAUTHORIZED));

			} catch (Exception e) {
				return complete(
						HttpResponse.create().withStatus(StatusCodes.INTERNAL_SERVER_ERROR).withEntity(e.getMessage()));
			}
		}).orElse(complete(HttpResponse.create().withStatus(StatusCodes.UNAUTHORIZED)));
	}

	private UserData fromJwsToken(String jwtToken) {
		Key key = getSecurityKey();

		Jws<Claims> jwt = Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken);

		Claims body = jwt.getBody();

		String email = body.get(USER_EMAIL).toString();
		UUID userId = UUID.fromString(body.get(USER_ID).toString());

		return new UserData(userId, email, jwtToken);
	}

}
