package com.github.russp.jtorrt;

import com.github.russp.jtorrt.common.InfoHash;
import com.github.russp.jtorrt.common.Rpc;
import com.github.russp.jtorrt.common.TorrentData;
import com.github.russp.jtorrt.common.TorrentDetails;
import com.github.russp.jtorrt.common.TorrentMetaData;
import com.github.russp.jtorrt.common.Tracker;
import com.github.russp.jtorrt.rpc.ClientService;
import com.github.russp.jtorrt.rpc.qbittorrent.QbittorrentConfig;
import com.github.russp.jtorrt.rpc.qbittorrent.QbittorrentRpc;
import com.github.russp.jtorrt.tracker.RuTracker;
import com.github.russp.jtorrt.tracker.RuTrackerConfig;
import com.github.russp.jtorrt.tracker.TrackerService;
import io.helidon.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateServiceTest {

	@Mock
	ClientService clientService;

	@Mock
	TrackerService trackerService;

	@Mock
	Rpc rpcMock;

	@Mock
	Tracker trackerMock;

	@InjectMocks
	UpdateService sut;

	@Test
	void testRun() throws Throwable {
		var oldHash = new InfoHash("A");
		var newHash = new InfoHash("B");
		var torrentData = new TorrentData(oldHash, "TestTorrent", 123);
		var torrentMetaData = new TorrentMetaData(new byte[]{});
		var url = "https://TestTorrent";

		when(clientService.getClients()).thenReturn(List.of(rpcMock));

		when(rpcMock.getTorrents()).thenReturn(List.of(torrentData));
		when(rpcMock.getTorrentDetails(torrentData)).thenReturn(new TorrentDetails(torrentData, url));

		when(trackerService.getTrackers()).thenReturn(List.of(trackerMock));
		when(trackerMock.supports(url)).thenReturn(true);
		when(trackerMock.getHash(url)).thenReturn(newHash);
		when(trackerMock.getTorrent(url)).thenReturn(torrentMetaData);

		sut.run(null);

		verify(rpcMock).replaceTorrent(oldHash, torrentMetaData);
	}
}