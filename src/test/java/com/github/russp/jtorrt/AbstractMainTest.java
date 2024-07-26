package com.github.russp.jtorrt;


import com.github.russp.jtorrt.app.Routing;
import com.github.russp.jtorrt.http.ClientFacade;
import com.github.russp.jtorrt.http.TorrentsFacade;
import com.github.russp.jtorrt.http.TrackerFacade;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

abstract class AbstractMainTest {
	private final Http1Client client;

	protected AbstractMainTest(Http1Client client) {
		this.client = client;
	}

	@SetUpRoute
	static void routing(HttpRouting.Builder builder) {
		new Routing(mock(ClientFacade.class), mock(TorrentsFacade.class), mock(TrackerFacade.class))
				.accept(builder);
	}

	@Test
	void testMetricsObserver() {
		try (Http1ClientResponse response = client.get("/observe/metrics").request()) {
			assertThat(response.status(), is(Status.OK_200));
		}
	}

}
