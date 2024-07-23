package com.github.russp.jtorrt.app;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.helidon.config.Config;
import io.helidon.faulttolerance.BulkheadException;
import io.helidon.faulttolerance.CircuitBreakerOpenException;
import io.helidon.faulttolerance.TimeoutException;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.Status;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.staticcontent.StaticContentService;
import jakarta.inject.Inject;

@Factory
public class JTorrtAppWebServerFactory {

	private final Config config;
	private final Routing routing;

	@Inject
	public JTorrtAppWebServerFactory(Config config, Routing routing) {
		this.config = config;
		this.routing = routing;
	}

	@Bean
	public WebServer webServer() {
		return WebServer.builder()
				.config(config.get("server"))
				.routing(JTorrtAppWebServerFactory::baseRouting)
				.routing(routing)
				.build();
	}

	/**
	 * Updates HTTP Routing.
	 */
	static void baseRouting(HttpRouting.Builder routing) {
		routing
				.error(BulkheadException.class,
						(req, res, ex) -> res.status(Status.SERVICE_UNAVAILABLE_503).send("bulkhead"))
				.error(CircuitBreakerOpenException.class,
						(req, res, ex) -> res.status(Status.SERVICE_UNAVAILABLE_503).send("circuit breaker"))
				.error(TimeoutException.class,
						(req, res, ex) -> res.status(Status.REQUEST_TIMEOUT_408).send("timeout"))
				.error(Throwable.class,
						(req, res, ex) -> res.status(Status.INTERNAL_SERVER_ERROR_500)
								.send(ex.getClass().getName() + ": " + ex.getMessage()))
				.any("/", (req, res) -> {
					res.status(Status.MOVED_PERMANENTLY_301);
					res.header(HeaderValues.createCached(HeaderNames.LOCATION, "/ui"));
					res.send();
				})
				.register("/ui", StaticContentService.builder("WEB")
						.welcomeFileName("index.html")
						.build());
	}
}
