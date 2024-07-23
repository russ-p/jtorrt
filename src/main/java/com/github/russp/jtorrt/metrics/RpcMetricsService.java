package com.github.russp.jtorrt.metrics;

import com.github.russp.jtorrt.common.RpcMetricSource;
import com.github.russp.jtorrt.common.RpcMetricValue;
import com.github.russp.jtorrt.common.RpcMetrics;
import com.github.russp.jtorrt.rpc.ClientService;
import io.avaje.inject.PostConstruct;
import io.helidon.metrics.api.FunctionalCounter;
import io.helidon.metrics.api.Gauge;
import io.helidon.metrics.api.MeterRegistry;
import io.helidon.metrics.api.Metrics;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class RpcMetricsService {

	private static final Logger log = LoggerFactory.getLogger(RpcMetricsService.class);

	private static final String SCOPE = "rpc";
	private static final String TAG_TYPE = "type";
	private static final String TAG_SERVER = "server";

	private final ClientService clientService;
	private final MeterRegistry globalRegistry;

	private final Map<RpcMetricSource, RpcMetricHolder> map = new ConcurrentHashMap<>();

	@Inject
	public RpcMetricsService(ClientService clientService, MeterRegistry globalRegistry) {
		this.clientService = clientService;
		this.globalRegistry = globalRegistry;
	}

	@PostConstruct
	public void init() {
		for (RpcMetricSource source : clientService.getMetrics()) {
			globalRegistry
					.getOrCreate(FunctionalCounter.builder(MetricNames.ENABLED, source, this::adaptFn)
							.description(MetricNames.ENABLED_DESC)
							.tags(Metrics.tags(TAG_TYPE, source.getType()))
							.scope(SCOPE)
					);
			adaptFn(source);
		}
	}

	private Long adaptFn(RpcMetricSource source) {
		if (source.getInstance() == null || source.getInstance().isEmpty()) {
			return 0L;
		}
		try {
			var metricHolder = map.compute(source, (src, v) -> v == null ? new RpcMetricHolder(src.getMetrics()) : v.setRpcMetrics(src.getMetrics()));
			write(globalRegistry, source.getType(), source.getInstance(), metricHolder);
			return 1L;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return 0L;
		}
	}

	private void write(MeterRegistry registry, String serverType, String server, RpcMetrics value) {
		registry.getOrCreate(Gauge
				.builder(MetricNames.DL_INFO_DATA, value::dlData)
				.description(MetricNames.DL_INFO_DATA_DESC)
				.tags(Metrics.tags(TAG_TYPE, serverType, TAG_SERVER, server))
				.scope(SCOPE));
		registry.getOrCreate(Gauge
				.builder(MetricNames.UP_INFO_DATA, value::upData)
				.description(MetricNames.UP_INFO_DATA_DESC)
				.tags(Metrics.tags(TAG_TYPE, serverType, TAG_SERVER, server))
				.scope(SCOPE));
		registry.getOrCreate(Gauge
				.builder(MetricNames.ALLTIME_DL, value::allTimeDl)
				.description(MetricNames.ALLTIME_DL_DESC)
				.tags(Metrics.tags(TAG_TYPE, serverType, TAG_SERVER, server))
				.scope(SCOPE));
		registry.getOrCreate(Gauge
				.builder(MetricNames.ALLTIME_UL, value::allTimeUp)
				.description(MetricNames.ALLTIME_UL_DESC)
				.tags(Metrics.tags(TAG_TYPE, serverType, TAG_SERVER, server))
				.scope(SCOPE));
		registry.getOrCreate(Gauge
				.builder(MetricNames.DHT_NODES, value::dhtNodes)
				.description(MetricNames.DHT_NODES_DESC)
				.tags(Metrics.tags(TAG_TYPE, serverType, TAG_SERVER, server))
				.scope(SCOPE));

		for (RpcMetricValue.State state : RpcMetrics.State.values()) {
			for (String category : value.categories()) {
				if (value.count(category, state) > 0) {
					registry.getOrCreate(Gauge
							.builder(MetricNames.TORRENTS_COUNT, () -> value.count(category, state))
							.description(MetricNames.TORRENTS_COUNT_DESC)
							.tags(Metrics.tags(TAG_TYPE, serverType,
									TAG_SERVER, server,
									"state", state.toString(),
									"category", category))
							.scope(SCOPE));
				}
			}
		}
	}

}
