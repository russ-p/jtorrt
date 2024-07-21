package com.github.russp.jtorrt;

import com.github.russp.jtorrt.common.InfoHash;
import com.github.russp.jtorrt.common.Rpc;
import com.github.russp.jtorrt.common.TorrentData;
import com.github.russp.jtorrt.common.TorrentMetaData;
import com.github.russp.jtorrt.common.Tracker;
import com.github.russp.jtorrt.rpc.ClientService;
import com.github.russp.jtorrt.tracker.TrackerService;
import com.github.russp.jtorrt.util.Tuple;
import io.helidon.scheduling.FixedRateInvocation;
import io.helidon.scheduling.ScheduledConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class UpdateService implements ScheduledConsumer<FixedRateInvocation> {

	private static final Logger log = LoggerFactory.getLogger(UpdateService.class);

	private final Map<InfoHash, LocalDateTime> lastUpdated = new HashMap<>();
	private final ClientService clientService;
	private final TrackerService trackerService;

	public UpdateService(ClientService clientService, TrackerService trackerService) {
		this.clientService = clientService;
		this.trackerService = trackerService;
	}

	@Override
	public void run(FixedRateInvocation invocation) throws Throwable {
		clientService.getClients().forEach(client -> run(client, trackerService.getTrackers()));
	}

	private void run(Rpc client, List<Tracker> trackers) {
		client.getTorrents().stream()
				.filter(torrent -> !lastUpdated.containsKey(torrent.hash()) || lastUpdated.get(torrent.hash()).isBefore(LocalDateTime.now().minusHours(1)))
				.map(client::getTorrentDetails)
				// resolve tracker
				.map(torrent -> trackers.stream()
						.filter(tracker -> tracker.supports(torrent.comment()))
						.findFirst()
						.map(tracker -> Tuple.of(torrent, tracker)))
				.flatMap(Optional::stream)
				// get hash from tracker
				.flatMap(pair -> getNewHash(pair.b(), pair.a().comment()).map(pair::concat))
				// check changes
				.filter(triplet -> !triplet.a().main().hash().equals(triplet.c()))
				.peek(triplet -> log.info("Torrent '{}': old={}, new={}", triplet.a().main().name(), triplet.a().main().hash(), triplet.c()))
				// download torrent file
				.flatMap(triplet -> downloadTorrent(triplet.b(), triplet.a().comment()).map(torrentMetaData -> Tuple.of(triplet.a().main(), triplet.c(), torrentMetaData)))
				// add new torrent
				.forEach(triplet -> replaceTorrent(client, triplet.a(), triplet.b(), triplet.c()));
	}

	private Stream<TorrentMetaData> downloadTorrent(Tracker tracker, String url) {
		try {
			log.debug("Download torrent from '{}'", url);
			return Stream.of(tracker.getTorrent(url));
		} catch (Exception e) {
			log.error("Can't download torrent '{}':", url, e);
			return Stream.empty();
		}
	}

	private void replaceTorrent(Rpc client, TorrentData mainData, InfoHash newHash, TorrentMetaData metaData) {
		try {
			var oldHash = mainData.hash();
			log.debug("Replace '{}' to new hash '{}'", mainData.name(), newHash.value());
			client.replaceTorrent(oldHash, metaData);
			lastUpdated.put(oldHash, LocalDateTime.now());
			lastUpdated.put(newHash, LocalDateTime.now());
			log.info("Added '{}' with new hash '{}'", mainData.name(), newHash.value());
		} catch (Exception e) {
			log.error("Can't add torrent '{}': ", mainData.name(), e);
		}
	}

	private Stream<InfoHash> getNewHash(Tracker tracker, String url) {
		try {
			log.debug("Get new hash for '{}'", url);
			return Stream.of(tracker.getHash(url));
		} catch (Exception e) {
			log.error("Torrent '{}': ", url, e);
			return Stream.empty();
		}
	}
}
