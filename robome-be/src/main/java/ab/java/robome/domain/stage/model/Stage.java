package ab.java.robome.domain.stage.model;

import java.time.LocalDateTime;

import ab.java.robome.domain.table.model.TableState;
import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class Stage {

	private StageId stageId;
	private String name;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

}
