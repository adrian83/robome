package ab.java.robome.domain.stage.model;

import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableStageId.class)
@JsonDeserialize(as = ImmutableStageId.class)
public interface StageId {

	UUID id();
	
	UUID tableId();
}
