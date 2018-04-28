package ab.java.robome.domain.table.model;

import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@Value.Immutable
@JsonSerialize(as = ImmutableTableId.class)
@JsonDeserialize(as = ImmutableTableId.class)
public interface TableId {

	UUID tableId();

}
