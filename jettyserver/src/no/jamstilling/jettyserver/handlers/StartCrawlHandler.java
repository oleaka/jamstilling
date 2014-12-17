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

	        String domain = request.getParameter("domain");
    		String exceptions = request.getParameter("exceptions");

	        try {
	        	JettyServer.getCrawlManager().startCrawl(domain);
	        	response.getWriter().println(getOverviewText(domain));//"<html><body>Startet undersøkelse av " + domain + "</body></html>");
			} catch (Exception e) {
				response.getWriter().println("<html><body>Noe gikk galt.<p>");
				response.getWriter().println(e.getMessage());
				response.getWriter().println("</body></html>");
			}	        
    	} 
	}

	private String getOverviewText(String domain) {
		String html = "<html><body>Startet undersøkelse av " + domain + "<p>";
		
		html += "<form method=POST action=\"crawlsfordomain?domain="+domain +"\"><input type=hidden name=review value=\"2\"><input type=submit value=\"Til oversikt\"></form>";
		html += "</body></html>";
		return html;
		// crawlsfordomain?domain=computass.no
	}
}
