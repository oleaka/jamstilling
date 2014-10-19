package no.jamstilling.jettyserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import no.jamstilling.jettyserver.handlers.FileContentHandler;
import no.jamstilling.jettyserver.handlers.PageNotFoundHandler;
import no.jamstilling.jettyserver.handlers.ShowResultHandler;
import no.jamstilling.jettyserver.handlers.StartCrawlHandler;
import no.jamstilling.jettyserver.parser.CrawlManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;

public class JettyServer {
	
	static final Logger logger = LogManager.getLogger(JettyServer.class.getName());
	
	private int port = 8081;
	
	
	private static CrawlManager crawlManager = null;
	
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
	
		FileContentHandler testPage = new FileContentHandler("/test", "pages/test.html");
		
		FileContentHandler frontPage = new FileContentHandler("/", "pages/frontpage.html");
		StartCrawlHandler startCrawlHandler = new StartCrawlHandler("/crawl_start");
		ShowResultHandler resultHander = new ShowResultHandler("/result", "pages/result.html");

		PageNotFoundHandler pageNotFound = new PageNotFoundHandler("pages/pagenotfound.html");
		
		handlers.addHandler(testPage);
		handlers.addHandler(frontPage);
		handlers.addHandler(startCrawlHandler);
		handlers.addHandler(resultHander);
		handlers.addHandler(pageNotFound);
		
		server.setHandler(handlers);
		server.start();
		server.join();
	}
	
	public synchronized static CrawlManager getCrawlManager() {
		if(crawlManager == null) {
			crawlManager = new CrawlManager();
		}
		return crawlManager;
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
