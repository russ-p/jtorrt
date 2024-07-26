package com.github.russp.jtorrt.http;

import com.github.russp.jtorrt.rpc.ClientService;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

abstract class AbstractClientFacadeTest {

	private static final ClientService CLIENT_SERVICE = mock(ClientService.class);

	private final Http1Client client;

	protected AbstractClientFacadeTest(Http1Client client) {
		this.client = client;
	}

	@SetUpRoute
	static void routing(HttpRouting.Builder builder) {
		builder.register("/api", new ClientFacade(CLIENT_SERVICE));
	}

	@AfterEach
	void tearDown() {
		Mockito.clearInvocations(CLIENT_SERVICE);
	}

	@Test
	void testGetTorrents() {
		try (Http1ClientResponse response = client.get("/api/clients/torrents").request()) {
			assertThat(response.status(), is(Status.OK_200));
		}
	}

	@Test
	void testAddTorrent() {
		try (Http1ClientResponse response = client.post("/api/clients/torrents").request()) {
			assertThat(response.status(), is(Status.NO_CONTENT_204));
		}
	}

	@Test
	void testRemoveTorrent() {
		try (Http1ClientResponse response = client.delete("/api/clients/torrents").request()) {
			assertThat(response.status(), is(Status.NO_CONTENT_204));
		}
	}

	@Test
	void testUpdateQbittorrent() {
		var body = new ClientFacade.QBitConfDto("https://localhost.dev", "testLogin", "test");
		try (Http1ClientResponse response = client.put("/api/clients/qbittorrent").submit(body)) {
			assertThat(response.status(), is(Status.NO_CONTENT_204));
			verify(CLIENT_SERVICE).setQbittorrentConfig("https://localhost.dev", "testLogin", "test");
		}
	}

}
