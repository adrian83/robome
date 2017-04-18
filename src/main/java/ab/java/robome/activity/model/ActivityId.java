package ab.java.robome.activity.model;

import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableActivityId.class)
@JsonDeserialize(as = ImmutableActivityId.class)
public interface ActivityId {

	UUID id();
	
	UUID tableId();
	
	UUID stageId();
	
}
