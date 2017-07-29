package ab.java.robome.web.security;

import java.security.Key;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.crypto.spec.SecretKeySpec;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class SecurityUtils extends AllDirectives {

	private Config config;

	@Inject
	public SecurityUtils(Config config) {
		this.config = config;
	}

	public Key getSecurityKey() {
		return new SecretKeySpec(config.getString("security.key").getBytes(), SignatureAlgorithm.HS512.getValue());
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
				UserData userData = fromJwsToken(jwtToken);
				return inner.apply(userData);
				
			} catch (SignatureException e) {
				return complete(HttpResponse.create()
						.withStatus(StatusCodes.UNAUTHORIZED));
				
			} catch (Exception e) {
				return complete(
						HttpResponse.create()
						.withStatus(StatusCodes.INTERNAL_SERVER_ERROR)
						.withEntity(e.getMessage()));
			}
		})
				.orElse(complete(HttpResponse.create().withStatus(StatusCodes.UNAUTHORIZED)));
	}

	private UserData fromJwsToken(String jwtToken) {
		Key key = getSecurityKey();
		
		Jws<Claims> jwt = Jwts.parser()
				.setSigningKey(key)
				.parseClaimsJws(jwtToken);

		String email = jwt.getBody()
				.get("user_email")
				.toString();

		return ImmutableUserData.builder()
				.email(email)
				.build();
	}

}
