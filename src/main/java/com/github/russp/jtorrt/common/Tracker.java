package com.github.russp.jtorrt.common;

public interface Tracker {
	boolean supports(String url);

	InfoHash getHash(String url);

	TorrentMetaData getTorrent(String url);
}
