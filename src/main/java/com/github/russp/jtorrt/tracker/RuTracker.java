package com.github.russp.jtorrt.tracker;

import com.github.russp.jtorrt.common.Configurable;
import com.github.russp.jtorrt.common.TorrentMetaData;
import com.github.russp.jtorrt.common.InfoHash;
import com.github.russp.jtorrt.common.Tracker;
import com.google.common.io.ByteStreams;
import io.helidon.http.HeaderNames;
import io.helidon.webclient.http1.Http1Client;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

public class RuTracker implements Tracker, Configurable<RuTrackerConfig> {

	private static final Logger log = LoggerFactory.getLogger(RuTracker.class);

	private static final Pattern RUTRACKER_TOPIC_PATTERN = Pattern.compile(".*t=(\\d+)");

	private final Http1Client client = Http1Client.builder()
			.baseUri("https://rutracker.org")
			.addHeader(HeaderNames.USER_AGENT, Const.USER_AGENT)
			.addHeader(HeaderNames.CACHE_CONTROL, "no-cache")
			.build();

	private RuTrackerConfig config;

	private boolean isLoggedIn = false;

	public RuTracker(RuTrackerConfig config) {
		this.config = config;
	}

	@Override
	public void configure(RuTrackerConfig config) {
		this.config = config;
		checkLogin();
	}

	@Override
	public boolean supports(String url) {
		return RUTRACKER_TOPIC_PATTERN.matcher(url).find();
	}

	@Override
	public InfoHash getHash(String url) {
		String result = client.get()
				.path("forum/viewtopic.php")
				.queryParam("t", getTopicId(url))
				.header(HeaderNames.COOKIE, this.config.cookie())
				.requestEntity(String.class);

		// <a href="magnet:?xt=..." class="med magnet-link" data-topic_id="" title="xxx">
		var hash = Jsoup.parse(result).select("a.magnet-link").attr("title");
		log.info("Received hash {} from url {}", hash, url);

		return new InfoHash(hash);
	}

	@Override
	public TorrentMetaData getTorrent(String url) {
		checkLogin();

		try (var response = client.get()
				.path("forum/dl.php")
				.queryParam("t", getTopicId(url))
				.header(HeaderNames.COOKIE, this.config.cookie())
				.request()) {
			if (response.status().code() == 200) {
				byte[] targetArray = ByteStreams.toByteArray(response.inputStream());
				log.info("Downloaded torrent from url {}", url);
				return new TorrentMetaData(targetArray);
			} else {
				log.error("Failed to download torrent from url {}, status {}", url, response.status().code());
				throw new RuntimeException("Failed to download file. Status code: " + response.status().code());
			}
		} catch (IOException e) {
			log.error("Failed to download torrent from url {}", url, e);
			throw new RuntimeException(e);
		}
	}

	private void checkLogin() {
		if (!isLoggedIn) {
			String result = client.get()
					.path("/forum/index.php")
					.header(HeaderNames.COOKIE, this.config.cookie())
					.requestEntity(String.class);
			// <a id="logged-in-username"
			var username = Jsoup.parse(result).select("a#logged-in-username").text();
			if (!username.isEmpty()) {
				log.info("Logged in user: {}", username);
				isLoggedIn = true;
			} else {
				throw new IllegalStateException("Login failed");
			}
		}
	}

	private static String getTopicId(String url) {
		var b = RUTRACKER_TOPIC_PATTERN.matcher(url);
		if (b.find()) {
			return b.group(1);
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}
}