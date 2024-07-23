package com.github.russp.jtorrt.app;

import com.github.russp.jtorrt.InMemStorage;
import com.github.russp.jtorrt.common.Storage;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.helidon.config.Config;
import io.helidon.metrics.api.MeterRegistry;
import io.helidon.metrics.api.Metrics;

@Factory
public class JTorrtAppConfiguration {

	@Bean
	public Config config() {
		// initialize global config from default configuration
		Config config = Config.create();
		Config.global(config);
		return config;
	}

	@Bean
	public MeterRegistry globalRegistry() {
		return Metrics.globalRegistry();
	}

	@Bean
	public Storage storage() {
		return new InMemStorage();
	}
}
