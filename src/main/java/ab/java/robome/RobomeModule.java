package ab.java.robome;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class RobomeModule extends AbstractModule {
	
	@Override
	protected void configure() {
		initializeConfig();
	}

	private void initializeConfig() {
		Config config = ConfigFactory.load();
		this.bind(Config.class).toInstance(config);
	}

}