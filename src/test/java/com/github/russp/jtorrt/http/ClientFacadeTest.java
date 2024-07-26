package com.github.russp.jtorrt.http;

import io.helidon.webserver.testing.junit5.DirectClient;
import io.helidon.webserver.testing.junit5.RoutingTest;

@RoutingTest
class ClientFacadeTest extends AbstractClientFacadeTest {
	ClientFacadeTest(DirectClient client) {
		super(client);
	}
}