package com.github.russp.jtorrt.rpc.qbittorrent;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

import java.util.ArrayList;

@CustomAdapter
public class TorrentInfoListCustomJsonAdapter implements JsonAdapter<TorrentInfoList> {

	private final JsonAdapter<ArrayList<TorrentInfo>> adapter;

	public TorrentInfoListCustomJsonAdapter(Jsonb jsonb) {
		this.adapter = jsonb.adapter(Types.listOf(TorrentInfo.class));
	}

	@Override
	public void toJson(JsonWriter writer, TorrentInfoList value) {
		adapter.toJson(writer, value);
	}

	@Override
	public TorrentInfoList fromJson(JsonReader reader) {
		var torrentInfos = new TorrentInfoList();
		torrentInfos.addAll(adapter.fromJson(reader));
		return torrentInfos;
	}
}
