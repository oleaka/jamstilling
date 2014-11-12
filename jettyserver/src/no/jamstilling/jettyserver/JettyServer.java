package no.jamstilling.jettyserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import no.jamstilling.jettyserver.handlers.FileContentHandler;
import no.jamstilling.jettyserver.handlers.PageNotFoundHandler;
import no.jamstilling.jettyserver.handlers.ResourceHandler;
import no.jamstilling.jettyserver.handlers.ShowAllDomainsHandler;
import no.jamstilling.jettyserver.handlers.ShowCrawlsForDomain;
import no.jamstilling.jettyserver.handlers.ShowResultHandler;
import no.jamstilling.jettyserver.handlers.ShowResultPageHandler;
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
	
	private static Map<String, String> language = new HashMap<String, String>();
	
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
		readLanguage();
		startServer();
	}

	private void startServer() throws Exception {
		Server server = new Server(port);		 
		HandlerList handlers = new HandlerList();
	
		FileContentHandler testPage = new FileContentHandler("/test", "pages/test.html");
	
		FileContentHandler frontPage = new FileContentHandler("/", "pages/frontpage.html");
		ShowAllDomainsHandler allDomainsPage = new ShowAllDomainsHandler("/alldomains", "pages/alldomains.html");
		StartCrawlHandler startCrawlHandler = new StartCrawlHandler("/crawl_start");
		ShowCrawlsForDomain crawlsForDomain = new ShowCrawlsForDomain("/crawlsfordomain", "pages/crawlsfordomain.html");
		ShowResultHandler resultHander = new ShowResultHandler("/result", "pages/result.html");
		ShowResultPageHandler resultPageHander = new ShowResultPageHandler("/resultpage", "pages/resultpage.html");
		
		ResourceHandler resourceHandler = new ResourceHandler();
		PageNotFoundHandler pageNotFound = new PageNotFoundHandler("pages/pagenotfound.html");
		
		handlers.addHandler(testPage);
		handlers.addHandler(allDomainsPage);
		handlers.addHandler(frontPage);
		handlers.addHandler(startCrawlHandler);
		handlers.addHandler(crawlsForDomain);
		handlers.addHandler(resultHander);
		handlers.addHandler(resultPageHander);
		handlers.addHandler(resourceHandler);
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

	private void readLanguage() {
		Properties prop = new Properties();
		
		try {
		
			InputStream inputStream = new FileInputStream("language.properties");
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			   
			prop.load(reader);
	 		
	 		Enumeration e = prop.propertyNames();

	 	    while (e.hasMoreElements()) {
	 	      String key = (String) e.nextElement();
	 	    
	 	      language.put(key, prop.getProperty(key));
	 	    }
	 		
	 		
	 		reader.close();
	 		inputStream.close();
		} catch (IOException ex) {
			logger.error("Error reading config", ex);
		} 
	}
	
	public static String getLanguage(String key) {
		if(language.containsKey(key)) {
			return language.get(key);
		}
		return key;
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
