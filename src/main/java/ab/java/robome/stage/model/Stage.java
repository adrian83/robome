package ab.java.robome.stage.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ab.java.robome.table.model.TableState;

@Value.Immutable
@JsonSerialize(as = ImmutableStage.class)
@JsonDeserialize(as = ImmutableStage.class)
public interface Stage {

	UUID id();
	
	UUID tableId();

	String name();

	TableState state();

	LocalDateTime createdAt();

	LocalDateTime modifiedAt();

	
}
