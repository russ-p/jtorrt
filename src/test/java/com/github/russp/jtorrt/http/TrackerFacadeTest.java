package com.github.russp.jtorrt.http;

import io.helidon.webserver.testing.junit5.DirectClient;
import io.helidon.webserver.testing.junit5.RoutingTest;

@RoutingTest
public class TrackerFacadeTest extends AbstractTrackerFacadeTest {
	protected TrackerFacadeTest(DirectClient client) {
		super(client);
	}
}
