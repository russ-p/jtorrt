package com.github.russp.jtorrt;

import com.github.russp.jtorrt.app.JTorrtApp;
import com.github.russp.jtorrt.app.JTorrtAppInstance;
import com.github.russp.jtorrt.http.ClientFacade;
import com.github.russp.jtorrt.http.TorrentsFacade;
import com.github.russp.jtorrt.metrics.RpcMetricsService;
import com.github.russp.jtorrt.rpc.ClientService;
import com.github.russp.jtorrt.http.TrackerFacade;
import com.github.russp.jtorrt.tracker.TrackerService;
import io.helidon.config.spi.ConfigSource;
import io.helidon.logging.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.scheduling.Scheduling;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.http.Status;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.webserver.staticcontent.StaticContentService;
import io.helidon.faulttolerance.BulkheadException;
import io.helidon.faulttolerance.CircuitBreakerOpenException;
import io.helidon.faulttolerance.TimeoutException;

import java.util.concurrent.TimeUnit;

/**
 * The application main class.
 */
public class Main {

	/**
	 * Cannot be instantiated.
	 */
	private Main() {
	}

	/**
	 * Application main entry point.
	 *
	 * @param args command line arguments.
	 */
	public static void main(String[] args) {
		// load logging configuration
		LogConfig.configureRuntime();

		// initialize global config from default configuration
		Config config = Config.create();
		Config.global(config);

		var app = JTorrtAppInstance.builder()
				.storage(context -> new InMemStorage())
				.clientService(context -> new ClientService(context.storage(), config))
				.trackerService(context -> new TrackerService(context.storage(), config))
				.add(ClientFacade.class, context -> new ClientFacade(context.clientService()))
				.add(TorrentsFacade.class, context -> new TorrentsFacade())
				.add(TrackerFacade.class, context -> new TrackerFacade(context.trackerService()))
				.add(UpdateService.class, context -> new UpdateService(context.clientService(), context.trackerService()))
				.add(RpcMetricsService.class, context -> new RpcMetricsService(context.clientService()))
				.build();

		WebServer server = WebServer.builder()
				.config(config.get("server"))
				.routing(routing -> routing(routing, app))
				.build()
				.start();

		Scheduling.fixedRate()
				.initialDelay(config.get("app.update.initial-delay").asInt().orElse(10))
				.delay(config.get("app.update.delay").asInt().orElse(60))
				.timeUnit(TimeUnit.MINUTES)
				.task(app.updateService())
				.build();

		System.out.println("WEB server is up! http://localhost:" + server.port() + "/simple-greet");
	}

	/**
	 * Updates HTTP Routing.
	 */
	static void routing(HttpRouting.Builder routing, JTorrtApp app) {
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
						.build())
				.register("/api/clients", app.clientFacade())
				.register("/api/torrents", app.torrentsFacade())
				.register("/api/trackers", app.trackerFacade());
	}
}