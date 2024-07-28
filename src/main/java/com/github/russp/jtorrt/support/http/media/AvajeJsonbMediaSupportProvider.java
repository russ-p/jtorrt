package com.github.russp.jtorrt.support.http.media;

import io.helidon.common.Weighted;
import io.helidon.common.config.Config;
import io.helidon.http.media.MediaSupport;
import io.helidon.http.media.spi.MediaSupportProvider;

public class AvajeJsonbMediaSupportProvider implements MediaSupportProvider, Weighted {

	/**
	 * This class should be only instantiated as part of java {@link java.util.ServiceLoader}.
	 */
	@Deprecated
	public AvajeJsonbMediaSupportProvider() {
		super();
	}

	@Override
	public String configKey() {
		return "avaje-jsonb";
	}

	@Override
	public MediaSupport create(Config config, String name) {
		return AvajeJsonbSupport.create(config, name);
	}

	@Override
	public double weight() {
		return 20;
	}
}
