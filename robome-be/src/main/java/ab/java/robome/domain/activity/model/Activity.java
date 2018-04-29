package ab.java.robome.domain.activity.model;

import java.time.LocalDateTime;

import ab.java.robome.domain.table.model.TableState;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Activity {

	private ActivityId id;
	private String name;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

}
