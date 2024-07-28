package com.github.russp.jtorrt.rpc.qbittorrent;


import io.avaje.jsonb.Json;

import java.util.List;
import java.util.Map;

@Json
public record MainData(
		long rid,
		boolean full_update,
		Map<String, TorrentInfo> torrents,
		Map<String, Category> categories,
		List<String> tags,
		ServerState server_state
) {

	@Json
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

	@Json
	public record Category(String name, String savePath) {

	}

}
