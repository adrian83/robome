package ab.java.robome.domain.stage.model;

import java.time.LocalDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ab.java.robome.domain.table.model.TableState;


@Value.Immutable
@JsonSerialize(as = ImmutableStage.class)
@JsonDeserialize(as = ImmutableStage.class)
public interface Stage {

	StageId stageId();

	String name();

	TableState state();

	LocalDateTime createdAt();

	LocalDateTime modifiedAt();

	
}
