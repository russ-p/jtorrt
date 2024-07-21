package com.github.russp.jtorrt.common;

public interface RpcMetricSource {

	String getType();

	String getInstance();

	RpcMetricValue getMetrics();

}
