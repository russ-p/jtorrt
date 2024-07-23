package com.github.russp.jtorrt;


import com.github.russp.jtorrt.app.JTorrtAppWebServerFactory;
import com.github.russp.jtorrt.app.Routing;
import com.github.russp.jtorrt.http.ClientFacade;
import com.github.russp.jtorrt.http.TorrentsFacade;
import com.github.russp.jtorrt.http.TrackerFacade;
import io.helidon.http.Status;
import io.helidon.webclient.api.ClientResponseTyped;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.containsString;
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
	void testFileService() {
		try (Http1ClientResponse response = client.get("/api").request()) {
			assertThat(response.status(), is(Status.OK_200));
		}
	}


	@Test
	public void testPerformGetMethod() {
		String greeting = WebClientMain.performGetMethod(client);
		assertThat(greeting, containsString("Hello World!"));
	}


	@Test
	void testAsync() {
		String response = client.get()
				.path("/async")
				.request()
				.as(String.class);

		assertThat(response, is("blocked for 100 millis"));
	}

	@Test
	void testBulkhead() throws InterruptedException {
		// bulkhead is configured for limit of 1 and queue of 1, so third
		// request should fail
		client.get()
				.path("/bulkhead/10000")
				.request();

		client.get()
				.path("/bulkhead/10000")
				.request();

		// I want to make sure the above is connected
		Thread.sleep(300);

		try (Http1ClientResponse third = client.get().path("/bulkhead/10000").request()) {
			// registered an error handler in Main
			assertThat(third.status(), is(Status.OK_200));
			assertThat(third.as(String.class), is("Call rejected: 1"));
		}
	}

	@Test
	void testCircuitBreaker() {
		String response = client.get()
				.path("/circuitBreaker/true")
				.request()
				.as(String.class);

		assertThat(response, is("blocked for 100 millis"));

		// error ratio is 20% within 10 request
		client.get()
				.path("/circuitBreaker/false")
				.request()
				.close();

		// should work after first
		response = client.get()
				.path("/circuitBreaker/true")
				.request()
				.as(String.class);

		assertThat(response, is("blocked for 100 millis"));

		// should open after second
		client.get()
				.path("/circuitBreaker/false")
				.request()
				.close();

		Http1ClientResponse clientResponse = client.get()
				.path("/circuitBreaker/true")
				.request();

		// registered an error handler in Main
		assertThat(clientResponse.status(), is(Status.SERVICE_UNAVAILABLE_503));
		assertThat(clientResponse.as(String.class), is("circuit breaker"));
	}

	@Test
	void testFallback() {
		String response = client.get()
				.path("/fallback/true")
				.request()
				.as(String.class);

		assertThat(response, is("blocked for 100 millis"));

		response = client.get()
				.path("/fallback/false")
				.request()
				.as(String.class);

		assertThat(response, is("Failed back because of reactive failure"));
	}

	@Test
	void testRetry() {
		String response = client.get()
				.path("/retry/1")
				.request()
				.as(String.class);

		assertThat(response, is("calls/failures: 1/0"));

		response = client.get()
				.path("/retry/2")
				.request()
				.as(String.class);

		assertThat(response, is("calls/failures: 2/1"));

		response = client.get()
				.path("/retry/3")
				.request()
				.as(String.class);

		assertThat(response, is("calls/failures: 3/2"));

		try (Http1ClientResponse clientResponse = client.get()
				.path("/retry/4")
				.request()) {

			// no error handler specified
			assertThat(clientResponse.status(), is(Status.INTERNAL_SERVER_ERROR_500));
			assertThat(clientResponse.as(String.class), is("java.lang.RuntimeException: reactive failure"));
		}
	}

	@Test
	void testTimeout() {
		String response = client.get()
				.path("/timeout/10")
				.request()
				.as(String.class);

		assertThat(response, is("Slept for 10 ms"));

		try (Http1ClientResponse clientResponse = client.get()
				.path("/timeout/1000")
				.request()) {
			// error handler specified in Main
			assertThat(clientResponse.status(), is(Status.REQUEST_TIMEOUT_408));
			assertThat(clientResponse.as(String.class), is("timeout"));
		}
	}


	@Test
	void testMetricsObserver() {
		try (Http1ClientResponse response = client.get("/observe/metrics").request()) {
			assertThat(response.status(), is(Status.OK_200));
		}
	}


	@Test
	void testSimpleGreet() {
		ClientResponseTyped<String> response = client.get("/simple-greet").request(String.class);
		assertThat(response.status(), is(Status.OK_200));
		assertThat(response.entity(), is("Hello World!"));
	}


}
