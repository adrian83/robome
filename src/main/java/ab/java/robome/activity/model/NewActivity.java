package ab.java.robome.activity.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableNewActivity.class)
@JsonDeserialize(as = ImmutableNewActivity.class)
public interface NewActivity {
	
	String name();
	
	

}
