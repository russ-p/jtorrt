package com.github.russp.jtorrt.common;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface RpcMetrics {
	long dlData();

	long upData();

	long allTimeDl();

	long allTimeUp();

	long dhtNodes();

	List<RpcMetricValue.Count> counts();

	default Set<String> categories() {
		return counts().stream()
				.map(Count::category)
				.collect(Collectors.toSet());
	}

	default Long count(String category, RpcMetricValue.State state) {
		return counts().stream()
				.filter(c -> c.state() == state && Objects.equals(c.category(), category))
				.mapToLong(Count::count)
				.sum();
	}

	enum State {
		DOWNLOADING,
		SEEDING,
		STOPPED,
		UNKNOWN
	}

	record Count(String category, RpcMetricValue.State state, int count) {
	}
}
