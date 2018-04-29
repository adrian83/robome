package ab.java.robome.domain.table.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class TableId {

	private UUID tableId;

}
