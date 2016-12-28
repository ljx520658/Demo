package com.gitplex.symbolextractor.web;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Separate startup class for people that want to run the examples directly. Use
 * parameter -Dcom.sun.management.jmxremote to startup JMX (and e.g. connect
 * with jconsole).
 */
public class WebServer {
	/**
	 * Main function, starts the jetty server.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("wicket.configuration", "development");

		Server server = new Server();

		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(8443);
		http_config.setOutputBufferSize(32768);

		ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
		http.setPort(8080);
		http.setIdleTimeout(1000 * 60 * 60);

		server.addConnector(http);

		WebAppContext bb = new WebAppContext();
		bb.setServer(server);
		bb.setContextPath("/");
		bb.setWar("src/test/webapp");

		server.setHandler(bb);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}
}
