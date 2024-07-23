package com.github.russp.jtorrt.app;

import com.github.russp.jtorrt.http.ClientFacade;
import com.github.russp.jtorrt.http.TorrentsFacade;
import com.github.russp.jtorrt.http.TrackerFacade;
import io.helidon.webserver.http.HttpRouting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.function.Consumer;

@Singleton
public class Routing implements Consumer<HttpRouting.Builder> {

	private final ClientFacade clientFacade;
	private final TorrentsFacade torrentsFacade;
	private final TrackerFacade trackerFacade;

	@Inject
	public Routing(ClientFacade clientFacade, TorrentsFacade torrentsFacade, TrackerFacade trackerFacade) {
		this.clientFacade = clientFacade;
		this.torrentsFacade = torrentsFacade;
		this.trackerFacade = trackerFacade;
	}

	/**
	 * App specific HTTP Routing.
	 */
	@Override
	public void accept(HttpRouting.Builder routing) {
		routing.register("/api", clientFacade, torrentsFacade, trackerFacade);
	}
}
