package com.github.russp.jtorrt;

import io.avaje.inject.BeanScope;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;

/**
 * The application main class.
 */
public class Main {

	/**
	 * Cannot be instantiated.
	 */
	private Main() {
	}

	/**
	 * Application main entry point.
	 *
	 * @param args command line arguments.
	 */
	public static void main(String[] args) {
		// load logging configuration
		LogConfig.configureRuntime();

		try (BeanScope beanScope = BeanScope.builder().build()) {
			var server = beanScope.get(WebServer.class);
			server.start();
			System.out.println("WEB server is up! http://localhost:" + server.port() + "/");
		}
	}

}