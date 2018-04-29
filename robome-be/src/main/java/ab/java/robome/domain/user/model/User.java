package ab.java.robome.domain.user.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class User {
	
	private UUID id;
	private String email;
	private String passwordHash;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

}
