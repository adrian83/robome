package ab.java.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableTable.class)
@JsonDeserialize(as = ImmutableTable.class)
public interface Table {

	TableId id();
	
	UUID userId();

	String title();

	TableState state();

	LocalDateTime createdAt();

	LocalDateTime modifiedAt();

}
