package com.github.russp.jtorrt.rpc.qbittorrent;

import com.github.russp.jtorrt.common.TorrentMetaData;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Disabled
public class QbittorrentRpcIT {

	@RegisterExtension
	static WireMockExtension wm = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().dynamicHttpsPort()
					.networkTrafficListener(new ConsoleNotifyingWiremockNetworkTrafficListener(StandardCharsets.ISO_8859_1)))
			.build();

	@Test
	void testAddTorrents() throws IOException {
		wm.stubFor(WireMock.any(anyUrl())
				.atPriority(10)
				.willReturn(aResponse()
						.proxiedFrom("-")));

		var bytes = FileUtils.readFileToByteArray(new File("-"));

		var rpc = new QbittorrentRpc(new QbittorrentConfig(wm.getRuntimeInfo().getHttpBaseUrl(), "-", "-"));

		rpc.addTorrent(new TorrentMetaData(bytes));
	}

}
