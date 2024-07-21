package com.github.russp.jtorrt.http;

import com.github.russp.jtorrt.rpc.ClientService;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFacade implements HttpService {
	private static final Logger log = LoggerFactory.getLogger(ClientFacade.class);

	private final ClientService clientService;

	public ClientFacade(ClientService clientService) {
		this.clientService = clientService;
	}

	@Override
	public void routing(HttpRules httpRules) {
		httpRules.put("/qbittorrent", this::updateQbittorrent)
				.get("/torrents", this::getTorrents)
				.post("/torrent", this::addTorrent)
				.delete("/torrent", this::removeTorrent);
	}

	private void getTorrents(ServerRequest serverRequest, ServerResponse serverResponse) {
//		clientService.getTorrents();
	}

	private void addTorrent(ServerRequest serverRequest, ServerResponse serverResponse) {
	}

	private void removeTorrent(ServerRequest serverRequest, ServerResponse serverResponse) {
	}

	private void updateQbittorrent(ServerRequest serverRequest, ServerResponse serverResponse) {
		QBitConfDto dto = null;
		try {
			dto = serverRequest.content().as(QBitConfDto.class);
			clientService.setQbittorrentConfig(dto.baseUrl(), dto.login(), dto.password());
			log.info("QBittorrent settings updated");

			serverResponse.status(Status.NO_CONTENT_204)
					.send();
		} catch (Exception e) {
			log.warn("Can't parse body", e);
			serverResponse.status(Status.BAD_REQUEST_400)
					.send();
		}
	}

	public record QBitConfDto(String baseUrl, String login, String password) {
	}

}
