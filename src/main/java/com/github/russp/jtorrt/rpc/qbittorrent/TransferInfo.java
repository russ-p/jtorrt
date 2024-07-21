package com.github.russp.jtorrt.rpc.qbittorrent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferInfo(
		long dl_info_speed, //	Global download rate (bytes/s)
		long dl_info_data,  //	Data downloaded this session (bytes)
		long up_info_speed, //	Global upload rate (bytes/s)
		long up_info_data,  //	Data uploaded this session (bytes)
		long dl_rate_limit, //	Download rate limit (bytes/s)
		long up_rate_limit, //	Upload rate limit (bytes/s)
		long dht_nodes,    //	DHT nodes connected to
		String connection_status//	Connection status. See possible values here below
) {
}
