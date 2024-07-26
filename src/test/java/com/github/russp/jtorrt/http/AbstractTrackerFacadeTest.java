package com.github.russp.jtorrt.http;

import com.github.russp.jtorrt.common.InfoHash;
import com.github.russp.jtorrt.common.TorrentMetaData;
import com.github.russp.jtorrt.tracker.TrackerService;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

abstract class AbstractTrackerFacadeTest {

	private static final TrackerService TRACKER_SERVICE = mock(TrackerService.class);

	private final Http1Client client;

	protected AbstractTrackerFacadeTest(Http1Client client) {
		this.client = client;
	}

	@SetUpRoute
	static void routing(HttpRouting.Builder builder) {
		builder.register("/api", new TrackerFacade(TRACKER_SERVICE));
	}

	@AfterEach
	void tearDown() {
		Mockito.clearInvocations(TRACKER_SERVICE);
	}

	@Test
	void getInfoHash() {
		when(TRACKER_SERVICE.getInfoHash("https://localhast.dev/t.php"))
				.thenReturn(Optional.of(new InfoHash("xxx")));

		try (Http1ClientResponse response = client.get("/api/trackers/infoHash")
				.queryParam("url", "https://localhast.dev/t.php").request()) {
			assertThat(response.status(), is(Status.OK_200));
		}
	}

	@Test
	void updateRuTracker() {
		var body = new TrackerFacade.RuTrackerConfDto(true, "cookie");
		try (Http1ClientResponse response = client.put("/api/trackers/rutracker").submit(body)) {
			assertThat(response.status(), is(Status.NO_CONTENT_204));
			verify(TRACKER_SERVICE).setRuTrackerConfig("cookie");
		}
	}

	@Test
	void getTorrent() {
		when(TRACKER_SERVICE.getTorrent("https://localhast.dev/t.php"))
				.thenReturn(Optional.of(new TorrentMetaData(new byte[]{})));

		try (Http1ClientResponse response = client.get("/api/trackers/torrent")
				.queryParam("url", "https://localhast.dev/t.php").request()) {
			assertThat(response.status(), is(Status.OK_200));
		}
	}
}