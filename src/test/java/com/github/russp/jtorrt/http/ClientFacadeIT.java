package com.github.russp.jtorrt.http;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

@ServerTest
class ClientFacadeIT extends AbstractClientFacadeTest {
	ClientFacadeIT(Http1Client client) {
		super(client);
	}
}
