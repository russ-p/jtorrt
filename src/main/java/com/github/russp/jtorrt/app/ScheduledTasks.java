package com.github.russp.jtorrt.app;

import com.github.russp.jtorrt.UpdateService;
import io.avaje.inject.PostConstruct;
import io.helidon.config.Config;
import io.helidon.scheduling.Scheduling;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class ScheduledTasks {

	private static final AtomicBoolean initialized = new AtomicBoolean(false);

	private final Config config;
	private final UpdateService updateService;

	@Inject
	public ScheduledTasks(Config config, UpdateService updateService) {
		this.config = config;
		this.updateService = updateService;
	}

	@PostConstruct
	public void schedule() {
		// TODO: sometimes it's called twice...
		if (initialized.compareAndSet(false, true)) {
			Scheduling.fixedRate()
					.initialDelay(config.get("app.update.initial-delay").asInt().orElse(10))
					.delay(config.get("app.update.delay").asInt().orElse(60))
					.timeUnit(TimeUnit.MINUTES)
					.task(updateService)
					.build();
		}
	}
}
