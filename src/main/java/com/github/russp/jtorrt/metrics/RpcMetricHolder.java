package com.github.russp.jtorrt.metrics;

import com.github.russp.jtorrt.common.RpcMetricValue;
import com.github.russp.jtorrt.common.RpcMetrics;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

class RpcMetricHolder implements RpcMetrics {
	private final AtomicReference<RpcMetrics> source;

	RpcMetricHolder(RpcMetrics source) {
		this.source = new AtomicReference<>(source);
	}

	RpcMetricHolder setRpcMetrics(RpcMetrics newValue) {
		source.set(newValue);
		return this;
	}

	@Override
	public long dlData() {
		return source.get().dlData();
	}

	@Override
	public long upData() {
		return source.get().upData();
	}

	@Override
	public long allTimeDl() {
		return source.get().allTimeDl();
	}

	@Override
	public long allTimeUp() {
		return source.get().allTimeUp();
	}

	@Override
	public long dhtNodes() {
		return source.get().dhtNodes();
	}

	@Override
	public Set<String> categories() {
		return source.get().categories();
	}

	@Override
	public List<RpcMetricValue.Count> counts() {
		return source.get().counts();
	}

	@Override
	public Long count(String category, RpcMetricValue.State state) {
		return source.get().count(category, state);
	}
}
