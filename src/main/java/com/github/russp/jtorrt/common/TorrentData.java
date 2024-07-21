package com.github.russp.jtorrt.common;

public record TorrentData(
		InfoHash hash,
		String name,
		long totalSize
) {
}
