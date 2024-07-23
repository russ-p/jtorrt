package com.github.russp.jtorrt.http;

import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import jakarta.inject.Singleton;

@Singleton
public class TorrentsFacade implements HttpService {
	@Override
	public void routing(HttpRules httpRules) {

	}
}
