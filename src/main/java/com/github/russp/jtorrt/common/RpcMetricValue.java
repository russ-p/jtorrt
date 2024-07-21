package com.github.russp.jtorrt.common;

import java.util.List;

public record RpcMetricValue(long dlData,
                             long upData,
                             long allTimeDl,
                             long allTimeUp,
                             long dhtNodes,
                             List<Count> counts) implements RpcMetrics {

}
