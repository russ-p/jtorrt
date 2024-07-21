package com.github.russp.jtorrt.common;

import java.util.List;

public interface Rpc {

	List<TorrentData> getTorrents();

	TorrentDetails getTorrentDetails(TorrentData torrent);

	void replaceTorrent(InfoHash oldHash, TorrentMetaData torrent);
}
