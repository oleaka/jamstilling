package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.result.Crawl;

import org.eclipse.jetty.server.Request;

public class ShowAllDomainsHandler extends FileHandler {

	private final String urlToHandle;
	
	public ShowAllDomainsHandler(String urlToHandle, String fileName) {
		super(fileName);
		this.urlToHandle = urlToHandle;
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
    	if(urlToHandle.equals(target)) {
    		
    		response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	
    		try {
		        StorageHandler storage = new StorageHandler();
		        storage.connect();
		        
		        List<String> domains = storage.getAllDomains();
		        
		        String domainsText = createDomainsText(domains);
		        
		        String fileContent = getFileContent();
	
		        fileContent = fileContent.replaceAll("%DOMAINS%", domainsText);
	    	
		        response.getWriter().println(fileContent);
    		} catch (Exception e) {
    			response.getWriter().println(e.getMessage());
    		}
    	}
	}
	
	private String createDomainsText(List<String> domains) {
		if(domains.size() == 0) {
			return "Ingen registrerte domener";
		}
		Collections.sort(domains);
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<table border=\"1\">");

		buffer.append("<tr>");
	    buffer.append("<th>Domene</th>");
	    buffer.append("<th>Detaljar</th>");
		buffer.append("</tr>");
		
		for(String domain : domains) {
			buffer.append("<tr>");
			
			buffer.append("<td>" + domain + "</td>");			
			buffer.append("<td>" + "<form method=POST action=\"crawlsfordomain?domain="+domain+"\"><input type=hidden name=review value=\"2\"><input type=submit value=\"Se\"></form>" + "</td>");
			
			buffer.append("</tr>");
		}
		buffer.append("</table>");
		
		return buffer.toString();

		
	}
}
