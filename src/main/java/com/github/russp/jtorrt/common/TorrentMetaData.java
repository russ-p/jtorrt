package com.github.russp.jtorrt.common;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.BencodeInputStream;
import com.dampcake.bencode.Type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

public class TorrentMetaData {

	private final byte[] data;
	private Map<String, Object> map;

	public TorrentMetaData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public InfoHash infoHash() {
		return new InfoHash(calcInfoHash(data));
	}

	private Map<String, Object> decodedMap() {
		if (map == null) {
			var bencode = new Bencode(StandardCharsets.UTF_8, true);
			map = bencode.decode(data, Type.DICTIONARY);
		}
		return map;
	}

	private static String calcInfoHash(byte[] torrent) {
		var bencode = new Bencode(StandardCharsets.UTF_8, true);
		var map = bencode.decode(torrent, Type.DICTIONARY);
		var info = map.get("info");
		var infoBlock = bencode.encode((Map<?, ?>) info);

		try {
			var sha1 = MessageDigest.getInstance("SHA-1");
			sha1.update(infoBlock);
			return HexFormat.of().formatHex(sha1.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
