package com.github.russp.jtorrt.rpc;

import com.github.russp.jtorrt.common.Rpc;
import com.github.russp.jtorrt.common.RpcMetricSource;
import com.github.russp.jtorrt.common.Storage;
import com.github.russp.jtorrt.rpc.qbittorrent.QbittorrentConfig;
import com.github.russp.jtorrt.rpc.qbittorrent.QbittorrentRpc;
import io.helidon.config.Config;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class ClientService {

	private final Storage storage;

	private QbittorrentRpc qbittorrent;

	@Inject
	public ClientService(Storage storage, Config config) {
		this.storage = storage;
		this.qbittorrent = new QbittorrentRpc(new QbittorrentConfig(
				config.get("rpc.qbittorrent.baseUrl").asString().orElse(""),
				config.get("rpc.qbittorrent.login").asString().orElse(""),
				config.get("rpc.qbittorrent.password").asString().orElse("")
		));
	}

	public void setQbittorrentConfig(String baseUrl, String login, String password) {
		storage.put("rpc.qbittorrent.baseUrl", baseUrl);
		storage.put("rpc.qbittorrent.login", login);
		storage.put("rpc.qbittorrent.password", password);

		this.qbittorrent.configure(new QbittorrentConfig(baseUrl, login, password));
	}

	public List<Rpc> getClients() {
		return List.of(qbittorrent);
	}

	public List<RpcMetricSource> getMetrics() {
		return List.of(qbittorrent);
	}
}
