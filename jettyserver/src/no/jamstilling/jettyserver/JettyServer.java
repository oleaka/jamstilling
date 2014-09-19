package no.jamstilling.jettyserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

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
	
		server.setHandler(handlers);
		server.start();
		server.join();

	}
	
	private void readConfig() {
		File f = new File(".");
		System.out.println(f.getAbsolutePath());
		
		logger.trace("In readConfig as trace");
		Properties prop = new Properties();
		InputStream input = null;
	 
		logger.debug("In readConfig as debug");
		logger.error("In readConfig as error");
		logger.warn("In readConfig as warn");
		
		try {
	 		input = new FileInputStream("jettyserver.properties");
	 		prop.load(input);
	 		
	 		System.out.println(prop.getProperty("port"));
	
	 		port = Integer.parseInt(prop.getProperty("port"));
		
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}
	
}
