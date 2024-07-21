package com.github.russp.jtorrt.rpc.qbittorrent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MainData(
		long rid,
		boolean full_update,
		TorrentsMap torrents,
		CategoriesMap categories,
		TagList tags,
		ServerState server_state
) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ServerState(
			long alltime_dl,
			long alltime_ul,
			long average_time_queue,
			String connection_status,
			long dht_nodes,
			long dl_info_data,
			long dl_info_speed,
			long dl_rate_limit,
			long free_space_on_disk,
			String global_ratio,
			long queued_io_jobs,
			boolean queueing,
			String read_cache_hits,
			String read_cache_overload,
			long refresh_interval,
			long total_buffers_size,
			long total_peer_connections,
			long total_queued_size,
			long total_wasted_session,
			long up_info_data,
			long up_info_speed,
			long up_rate_limit,
			boolean use_alt_speed_limits,
			boolean use_subcategories,
			String write_cache_overload
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Category(String name, String savePAth) {

	}

	public static class TagList extends ArrayList<String> {
	}

	public static class CategoriesMap extends HashMap<String, Category> {
	}

	public static class TorrentsMap extends HashMap<String, TorrentInfo> {
	}

}
