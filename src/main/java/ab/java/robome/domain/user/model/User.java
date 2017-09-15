package ab.java.robome.domain.user.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@Value.Immutable
@JsonSerialize(as = ImmutableUser.class)
@JsonDeserialize(as = ImmutableUser.class)
public interface User {
	
	UUID id();
	
	String email();
	
	String passwordHash();

	LocalDateTime createdAt();

	LocalDateTime modifiedAt();

}
