package com.github.russp.jtorrt.rpc.qbittorrent;

import com.github.russp.jtorrt.util.Tuple;
import com.github.russp.jtorrt.common.Configurable;
import com.github.russp.jtorrt.common.InfoHash;
import com.github.russp.jtorrt.common.Rpc;
import com.github.russp.jtorrt.common.RpcMetricSource;
import com.github.russp.jtorrt.common.RpcMetricValue;
import com.github.russp.jtorrt.common.RpcMetrics;
import com.github.russp.jtorrt.common.TorrentData;
import com.github.russp.jtorrt.common.TorrentDetails;
import com.github.russp.jtorrt.common.TorrentMetaData;
import io.helidon.common.GenericType;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.http.WritableHeaders;
import io.helidon.http.media.MediaContext;
import io.helidon.http.media.multipart.MultiPartSupport;
import io.helidon.http.media.multipart.WriteableMultiPart;
import io.helidon.http.media.multipart.WriteablePart;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientRequest;
import io.helidon.webclient.http1.Http1ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class QbittorrentRpc implements Rpc, RpcMetricSource, Configurable<QbittorrentConfig> {
	private static final Logger log = LoggerFactory.getLogger(QbittorrentRpc.class);

	private final Http1Client client = Http1Client.builder()
			.addHeader(HeaderNames.USER_AGENT, "jtorrt")
			.sendExpectContinue(false) // not supported in qbit
			.maxInMemoryEntity(1024 * 1024) // chunked encoding isn't supported in qbit
			.cookieManager(b -> b.automaticStoreEnabled(true))
			.build();

	private QbittorrentConfig config;
	private boolean loggedIn = false;

	public QbittorrentRpc(QbittorrentConfig config) {
		this.config = config;
	}

	@Override
	public void configure(QbittorrentConfig config) {
		this.config = config;

		loggedIn = false;
		checkLogin();
	}

	@Override
	public List<TorrentData> getTorrents() {
		checkLogin();

		var response = makeGet("torrents", "info")
				.requestEntity(TorrentInfoList.class);

		return response.stream()
				.map(qbt -> new TorrentData(
						new InfoHash(qbt.hash()),
						qbt.name(),
						qbt.total_size()
				))
				.toList();
	}

	@Override
	public TorrentDetails getTorrentDetails(TorrentData torrent) {
		checkLogin();

		var qbt = makeGet("torrents", "properties")
				.queryParam("hash", torrent.hash().value())
				.requestEntity(TorrentProperties.class);

		return new TorrentDetails(torrent, qbt.comment());
	}

	public void addTorrent(TorrentMetaData torrent) {
		checkLogin();

		// torrent.infoHash()

		addTorrentInternal(torrent, "qqq", "", "");
	}

	public void removeTorrent() {
		checkLogin();

	}

	@Override
	public void replaceTorrent(InfoHash oldHash, TorrentMetaData torrent) {
		checkLogin();

		var newInfoHash = torrent.infoHash();
		if (oldHash.equals(newInfoHash)) {
			return;
		}

		var oldTorrentsList = makeGet("torrents", "info")
				.queryParam("hashes", oldHash.value() + "|" + newInfoHash.value())
				.requestEntity(TorrentInfoList.class);

		if (oldTorrentsList.isEmpty()) {
			throw new IllegalStateException("No torrent found for hash: " + oldHash);
		}
		if (oldTorrentsList.size() > 1) {
			log.info("Torrent for hash {} already found", oldHash);
			return;
		}

		var oldTorrent = oldTorrentsList.getFirst();
		addTorrentInternal(torrent, oldTorrent.name(), oldTorrent.save_path(), oldTorrent.category());
	}

	@Override
	public String getType() {
		return "qBittorrent";
	}

	@Override
	public String getInstance() {
		return this.config.baseUrl();
	}

	@Override
	public RpcMetricValue getMetrics() {
		checkLogin();
		if (!loggedIn) {
			return new RpcMetricValue(0, 0, 0, 0, 0, List.of());
		}
		var mainData = makeGet("sync", "maindata").requestEntity(MainData.class);

		var counts = mainData.torrents().values().stream()
				.map(torrentInfo -> new RpcMetricValue.Count(mapCategory(torrentInfo.category()), mapState(torrentInfo.state()), 1))
				.collect(Collectors.groupingBy(
						count -> Tuple.of(count.category(), count.state()),
						Collectors.reducing((a, b) -> new RpcMetricValue.Count(a.category(), a.state(), a.count() + b.count()))
				))
				.values()
				.stream()
				.flatMap(Optional::stream)
				.toList();

		return new RpcMetricValue(
				mainData.server_state().dl_info_data(),
				mainData.server_state().up_info_data(),
				mainData.server_state().alltime_dl(),
				mainData.server_state().alltime_ul(),
				mainData.server_state().dht_nodes(),
				counts
		);
	}

	private void addTorrentInternal(TorrentMetaData torrent, String torrentName, String savePath, String category) {
		var multiPart = WriteableMultiPart.builder();
		if (savePath != null && !savePath.isEmpty()) {
			multiPart.addPart(WriteablePart.builder("savepath").content(savePath).build());
		}
		if (category != null && !category.isEmpty()) {
			multiPart.addPart(WriteablePart.builder("category").content(category).build());
		}
		multiPart.addPart(
				WriteablePart.builder("torrents")
						.fileName(torrentName + ".torrent")
						.contentType(MediaTypes.create("application/x-bittorrent"))
						.content(torrent.getData())
						.build()
		);

		try (var response = makePost("torrents", "add", multiPart)) {
			if (response.status() == Status.OK_200) {
				var body = response.entity().as(String.class);
				if (!"Ok.".equals(body)) {
					throw new IllegalStateException("Add torrent error: " + body);
				}
				log.info("Added torrent {}", torrentName);
			} else {
				throw new IllegalStateException("Add torrent error: status = " + response.status());
			}
		}
	}

	private void checkLogin() {
		if (loggedIn) {
			return;
		}
		try (var response = makePost("auth", "login",
				Map.of("username", this.config.login(),
						"password", this.config.password()))) {
			if (response.status() == Status.OK_200) {
				loggedIn = true;
			} else {
				loggedIn = false;
				throw new IllegalStateException("Invalid login or password: " + response.status());
			}
		}
	}

	private Http1ClientRequest makeGet(String apiName, String methodName) {
		return client.get(this.config.baseUrl() + "/api/v2/" + apiName + "/" + methodName);
	}

	private Http1ClientResponse makePost(String apiName, String methodName, Map<String, Object> params) {
		return client.post(this.config.baseUrl() + "/api/v2/" + apiName + "/" + methodName)
				.contentType(MediaTypes.APPLICATION_FORM_URLENCODED)
				.submit(encodeFormParams(params));
	}

	private Http1ClientResponse makePost(String apiName, String methodName, WriteableMultiPart.Builder multiPartBuilder) {
		return client.post(this.config.baseUrl() + "/api/v2/" + apiName + "/" + methodName)
				.header(HeaderNames.CONTENT_LENGTH, String.valueOf(getContentLength(multiPartBuilder)))
				.submit(multiPartBuilder.build());
	}

	private static String encodeFormParams(Map<String, Object> params) {
		StringBuilder encodedParams = new StringBuilder();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (!encodedParams.isEmpty()) {
				encodedParams.append("&");
			}
			encodedParams.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
					.append("=")
					.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
		}
		return encodedParams.toString();
	}

	private static long getContentLength(WriteableMultiPart.Builder multiPartBuilder) {
		var baos = new ByteArrayOutputStream();
		var type = GenericType.create(WriteableMultiPart.class);
		WritableHeaders<?> headers = WritableHeaders.create();
		var mediaSupport = MultiPartSupport.create(null);
		mediaSupport.init(MediaContext.create());
		var writer = mediaSupport.writer(type, headers);
		writer.supplier().get().write(type, multiPartBuilder.build(), baos, headers);
		return baos.size();
	}

	private static RpcMetricValue.State mapState(String state) {
		return switch (state) {
			case "active" -> RpcMetrics.State.DOWNLOADING;
			case "uploading", "stalledUP" -> RpcMetrics.State.SEEDING;
			case "pausedUP", "queuedUP" -> RpcMetrics.State.STOPPED;
			case null, default -> RpcMetrics.State.UNKNOWN;
		};
	}

	private static String mapCategory(String category) {
		if (category == null || category.isEmpty()) {
			return "Uncategorized";
		}
		return category;
	}
}
