package ab.java.robome.domain.stage.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class StageId {

	UUID stageId;
	UUID tableId;
}
