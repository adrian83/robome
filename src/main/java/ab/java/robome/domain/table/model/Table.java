package ab.java.robome.domain.table.model;

import java.time.LocalDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableTable.class)
@JsonDeserialize(as = ImmutableTable.class)
public interface Table {

	TableId id();

	String name();

	TableState state();

	LocalDateTime createdAt();

	LocalDateTime modifiedAt();

}
