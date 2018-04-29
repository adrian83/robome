package ab.java.robome.domain.activity.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ActivityId  {

	private UUID activityId;
	private UUID tableId;
	private UUID stageId;
	
}
