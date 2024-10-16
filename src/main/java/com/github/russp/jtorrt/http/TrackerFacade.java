package com.github.russp.jtorrt.http;

import com.github.russp.jtorrt.tracker.TrackerService;
import io.avaje.jsonb.Json;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.json.JsonBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class TrackerFacade implements HttpService {
	private static final Logger log = LoggerFactory.getLogger(TrackerFacade.class);

	private final TrackerService trackerService;

	@Inject
	public TrackerFacade(TrackerService trackerService) {
		this.trackerService = trackerService;
	}

	@Override
	public void routing(HttpRules httpRules) {
		httpRules.put("/trackers/rutracker", this::updateRuTracker)
				.get("/trackers/infoHash", this::getInfoHash)
				.get("/trackers/torrent", this::getTorrent);
	}

	private void getInfoHash(ServerRequest serverRequest, ServerResponse serverResponse) {
		serverRequest.query().first("url")
				.asString()
				.asOptional()
				.flatMap(trackerService::getInfoHash)
				.ifPresentOrElse(value -> serverResponse
								.status(Status.OK_200)
								.send(value),
						() -> serverResponse
								.status(Status.BAD_REQUEST_400)
								.send());
	}

	private void updateRuTracker(ServerRequest serverRequest, ServerResponse serverResponse) {
		RuTrackerConfDto ruTrackerConfDto = null;
		try {
			ruTrackerConfDto = serverRequest.content().as(RuTrackerConfDto.class);
		} catch (Exception e) {
			log.warn("Can't parse body", e);
			serverResponse.status(Status.BAD_REQUEST_400)
					.send();
			return;
		}

		trackerService.setRuTrackerConfig(ruTrackerConfDto.cookie());
		log.info("Rutracker settings updated");

		serverResponse.status(Status.NO_CONTENT_204)
				.send();
	}

	private void getTorrent(ServerRequest serverRequest, ServerResponse serverResponse) {
		serverRequest.query().first("url")
				.asString()
				.asOptional()
				.flatMap(trackerService::getTorrent)
				.ifPresentOrElse(value -> serverResponse
								.status(Status.OK_200)
								.header(HeaderNames.CONTENT_TYPE, "application/x-bittorrent")
								.send(value.getData()),
						() -> serverResponse
								.status(Status.BAD_REQUEST_400)
								.send());
	}

	@Json
	public record RuTrackerConfDto(boolean enabled, String cookie) {
	}
}
