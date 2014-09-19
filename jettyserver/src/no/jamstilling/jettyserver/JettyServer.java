package no.jamstilling.jettyserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import no.jamstilling.jettyserver.handlers.FileContentHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;

public class JettyServer {
	
	static final Logger logger = LogManager.getLogger(JettyServer.class.getName());
	
	private int port = 8081;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new JettyServer();
		} catch (Exception e) {
			logger.error("Error starting jettyserver", e);
		}
	}

	public JettyServer() throws Exception {
		readConfig();
	
		
		startServer();
	}

	private void startServer() throws Exception {
		Server server = new Server(port);		 
		HandlerList handlers = new HandlerList();
	
		FileContentHandler frontPage = new FileContentHandler("/", "pages/frontpage.html");
		FileContentHandler pageNotFound = new FileContentHandler(null, "pages/pagenotfound.html");
		
		handlers.addHandler(frontPage);
		handlers.addHandler(pageNotFound);
		
		server.setHandler(handlers);
		server.start();
		server.join();

	}
	
	private void readConfig() {
		Properties prop = new Properties();
		
		try {
			InputStream input = new FileInputStream("jettyserver.properties");
	 		prop.load(input);
	
	 		port = Integer.parseInt(prop.getProperty("port"));
	 		
	 		input.close();
		} catch (IOException ex) {
			logger.error("Error reading config", ex);
		} 
	}
	
}
