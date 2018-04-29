package ab.java.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Table {

	private TableId id;
	private UUID userId;
	private String title;
	private String description;
	private TableState state;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

}
