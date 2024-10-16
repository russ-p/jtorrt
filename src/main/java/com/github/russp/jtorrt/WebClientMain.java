package com.github.russp.jtorrt;

import io.helidon.config.Config;
import io.helidon.config.ConfigValue;
import io.helidon.webclient.http1.Http1Client;


/**
 * A simple WebClient usage class.
 * <p>
 * Each of the methods demonstrates different usage of the WebClient.
 */
public class WebClientMain {

    private WebClientMain() {
    }

    /**
     * Executes WebClient examples.
     * <p>
     * If no argument provided it will take server port from configuration server.port.
     * <p>
     * User can override port from configuration by main method parameter with the specific port.
     *
     * @param args main method
     */
    public static void main(String[] args) {
        Config config = Config.global();
        String url;
        if (args.length == 0) {
            ConfigValue<Integer> port = config.get("server.port").asInt();
            if (!port.isPresent() || port.get() == -1) {
                throw new IllegalStateException("Unknown port! Please specify port as a main method parameter "
                        + "or directly to config server.port");
            }
            url = "http://localhost:" + port.get();
        } else {
            url = "http://localhost:" + Integer.parseInt(args[0]);
        }

        Http1Client client = Http1Client.builder()
                .baseUri(url)
                .build();

        performGetMethod(client);
    }

    static String performGetMethod(Http1Client client) {
        System.out.println("Get request execution.");
        String result = client.get().path("/simple-greet").requestEntity(String.class);
        System.out.println("GET request successfully executed.");
        System.out.println(result);
        return result;
    }

}
