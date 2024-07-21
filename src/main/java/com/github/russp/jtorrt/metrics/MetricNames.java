package com.github.russp.jtorrt.metrics;

interface MetricNames {

	String ENABLED = "rpc_metrics_ok";
	String ENABLED_DESC = "RPC metrics enabled";

	String DL_INFO_DATA = "dl_info_data";
	String DL_INFO_DATA_DESC = "Data downloaded since the server started, in bytes.";
	String UP_INFO_DATA = "up_info_data";
	String UP_INFO_DATA_DESC = "Data uploaded since the server started, in bytes.";
	String ALLTIME_UL = "alltime_ul";
	String ALLTIME_UL_DESC = "Total historical data uploaded, in bytes.";
	String ALLTIME_DL = "alltime_dl";
	String ALLTIME_DL_DESC = "Total historical data downloaded, in bytes.";
	String DHT_NODES = "dht_nodes";
	String DHT_NODES_DESC = "Number of DHT nodes connected to.";
	String TORRENTS_COUNT = "torrents_count";
	String TORRENTS_COUNT_DESC = "Number of torrents in a state under a category";

}
