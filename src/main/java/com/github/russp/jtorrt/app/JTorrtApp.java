package com.github.russp.jtorrt.app;

import com.github.russp.jtorrt.UpdateService;
import com.github.russp.jtorrt.common.Storage;
import com.github.russp.jtorrt.http.ClientFacade;
import com.github.russp.jtorrt.http.TorrentsFacade;
import com.github.russp.jtorrt.http.TrackerFacade;
import com.github.russp.jtorrt.metrics.RpcMetricsService;
import com.github.russp.jtorrt.rpc.ClientService;
import com.github.russp.jtorrt.tracker.TrackerService;

public interface JTorrtApp {

	Storage storage();

	ClientService clientService();

	TrackerService trackerService();

	UpdateService updateService();

	RpcMetricsService rpcMetricsService();

	ClientFacade clientFacade();

	TorrentsFacade torrentsFacade();

	TrackerFacade trackerFacade();

}
