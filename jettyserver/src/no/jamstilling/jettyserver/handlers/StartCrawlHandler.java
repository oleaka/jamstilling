package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.jettyserver.JettyServer;
import no.jamstilling.jettyserver.parser.CrawlManager;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class StartCrawlHandler extends AbstractHandler {

	private final String urlToHandle;
	
	public StartCrawlHandler(String urlToHandle) {
		this.urlToHandle = urlToHandle;
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
    	if(urlToHandle.equals(target)) {
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        
	        try {
				JettyServer.getCrawlManager().startCrawl("http://www.tidstankar.no");
			} catch (URISyntaxException e) {
		        response.getWriter().println("<html><body>Error starting crawl " + e.getMessage() + "</body></html>");

			}
	        
	        response.getWriter().println("<html><body>Started crawl</body></html>");
    	} 
	}

	
}
