package com.github.russp.jtorrt.rpc.qbittorrent;

import com.github.russp.jtorrt.common.InfoHash;
import com.github.russp.jtorrt.common.TorrentData;
import com.github.russp.jtorrt.common.TorrentMetaData;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class QbittorrentRpcTest {

	private QbittorrentRpc rpc;

	@BeforeEach
	void setup(WireMockRuntimeInfo wmRuntimeInfo) {
		rpc = new QbittorrentRpc(new QbittorrentConfig(wmRuntimeInfo.getHttpBaseUrl(), "test", "test"));

		stubFor(post(urlEqualTo("/api/v2/auth/login"))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	@Test
	void testGetTorrents() {
		stubFor(get(urlEqualTo("/api/v2/torrents/info"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBodyFile("torrent-info.json")
				));

		var torrents = rpc.getTorrents();

		assertThat(torrents).hasSize(2);
	}

	@Test
	void testGetTorrentDetails() {
		stubFor(get(urlEqualTo("/api/v2/torrents/properties?hash=2AA4F5A7E209E54B32803D43670971C4C8CAAA05"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBodyFile("torrent-properties.json")
				));

		var details = rpc.getTorrentDetails(new TorrentData(new InfoHash("2aa4f5a7e209e54b32803d43670971c4c8caaa05"), "Name", 123));

		assertThat(details.comment()).isEqualTo("Ubuntu CD releases.ubuntu.com");
	}

	@Test
	void testAddTorrent() {
		stubFor(post(urlEqualTo("/api/v2/torrents/add"))
				.withMultipartRequestBody(
						aMultipart()
								.withName("torrents")
								.withHeader("Content-Type", containing("application/x-bittorrent"))
				)
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("Ok.")));

		var data = new TorrentMetaData(new byte[]{127, 0, 0, 1});
		rpc.addTorrent(data);
	}

	@Test
	@Disabled
	void testRemoveTorrent() {
		rpc.removeTorrent();
	}

	@Test
	@Disabled
	void testReplaceTorrent() {
		var data = new TorrentMetaData(new byte[]{127, 0, 0, 1});
		rpc.replaceTorrent(new InfoHash("A"), data);
	}

	@Test
	void testGetMetrics() {
		stubFor(get(urlEqualTo("/api/v2/sync/maindata"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBodyFile("sync-maindata.json")
				));

		var rpcMetrics = rpc.getMetrics();

		assertThat(rpcMetrics.dlData()).isEqualTo(33061258437L);
		assertThat(rpcMetrics.upData()).isEqualTo(561101501467L);
	}
}