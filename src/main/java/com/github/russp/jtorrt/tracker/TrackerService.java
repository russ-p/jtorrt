package com.github.russp.jtorrt.tracker;

import com.github.russp.jtorrt.common.InfoHash;
import com.github.russp.jtorrt.common.Storage;
import com.github.russp.jtorrt.common.TorrentMetaData;
import com.github.russp.jtorrt.common.Tracker;
import io.helidon.config.Config;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class TrackerService {

	private final Storage storage;
	private final RuTracker ruTracker;

	@Inject
	public TrackerService(Storage storage, Config config) {
		this.storage = storage;
		this.ruTracker = new RuTracker(new RuTrackerConfig(
				config.get("tracker.rutracker.enabled").asBoolean().orElse(false),
				config.get("tracker.rutracker.cookie").asString().orElse("")
		));
	}

	public void setRuTrackerConfig(String cookie) {
		storage.put("tracker.rutracker.enabled", "true");
		storage.put("tracker.rutracker.cookie", cookie);
		this.ruTracker.configure(new RuTrackerConfig(true, cookie));
	}

	public Optional<InfoHash> getInfoHash(String url) {
		if (ruTracker.supports(url)) {
			return Optional.ofNullable(this.ruTracker.getHash(url));
		}
		return Optional.empty();
	}

	public Optional<TorrentMetaData> getTorrent(String url) {
		if (ruTracker.supports(url)) {
			return Optional.ofNullable(this.ruTracker.getTorrent(url));
		}
		return Optional.empty();
	}

	public List<Tracker> getTrackers() {
		return List.of(this.ruTracker);
	}
}
