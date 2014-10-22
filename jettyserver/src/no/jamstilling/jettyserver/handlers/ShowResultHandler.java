package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.text.DecimalFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.result.Crawl;
import no.jamstilling.mongo.result.CrawlResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

public class ShowResultHandler  extends FileHandler {

	static final DecimalFormat df = new DecimalFormat("#.00");
	static final Logger logger = LogManager.getLogger(ShowResultHandler.class.getName());
	
	private final String urlToHandle;
	
	public ShowResultHandler(String urlToHandle, String fileName) {
		super(fileName);
		this.urlToHandle = urlToHandle;
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
    	if(urlToHandle.equals(target)) {
    		
    		String domain = request.getParameter("domain");
    		String exceptions = request.getParameter("exceptions");
    		String crawlId = request.getParameter("crawlid");
    		
    		System.out.println(crawlId + " for " + domain + ", " + exceptions);
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        
	        try {
		        StorageHandler storage = new StorageHandler();
		        storage.connect(domain);
		        storage.continueCrawl(crawlId);

		        CrawlResult result = storage.getResult();
		        
		        String resultTable = createResultTable(result);
		        
		        String fileContent = getFileContent();
		        fileContent = fileContent.replaceAll("%DOMAIN%", domain);
		        fileContent = fileContent.replace("%RESULT_TABLE%", resultTable);
		        
		        response.getWriter().println(fileContent);
	        } catch (Exception e) {
	        	e.printStackTrace();
		        response.getWriter().println(e.getMessage());	        	
	        }
    	}
    	
	}
	
	private String createResultTable(CrawlResult result) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<table border=\"1\">");

		/*
		buffer.append("<tr>");
	    buffer.append("<th>Id</th>");
	    buffer.append("<th>Startet</th>");
	    buffer.append("<th>Ferdig</th>");
	    buffer.append("<th>Resultat</th>");
		buffer.append("</tr>");
		*/
		
		buffer.append("<tr>");
		buffer.append("<td>Startet</td>");
		buffer.append("<td>" + result.startTime + "</td>");
		buffer.append("</tr>");
		
		buffer.append("<tr>");
		buffer.append("<td>Ferdig</td>");
		buffer.append("<td>" + result.endTime + "</td>");
		buffer.append("</tr>");
		
		buffer.append("<tr>");
		buffer.append("<td>Antall sider</td>");
		buffer.append("<td>" + result.totalPages + "</td>");
		buffer.append("</tr>");

		buffer.append("<tr>");
		buffer.append("<td>Nynorsk</td>");
		buffer.append("<td>" + String.format( "%.2f", (((double) result.totalNNWords / (result.totalNNWords+result.totalBMWords))) * 100.0)+ "%</td>");
		buffer.append("</tr>");

		buffer.append("<tr>");
		buffer.append("<td>Bokmål</td>");
		buffer.append("<td>" + String.format( "%.2f", (((double) result.totalBMWords / (result.totalNNWords+result.totalBMWords))) * 100.0) + "%</td>");
		buffer.append("</tr>");

		
		/*
		for(Crawl crawl : list) {
			buffer.append("<tr>");
			
			buffer.append("<td>" + crawl.crawlId + "</td>");
			buffer.append("<td>" + crawl.started + "</td>");
			buffer.append("<td>" + crawl.ended + "</td>");
			buffer.append("<td>" + "<form method=POST action=\"result?domain="+domain+":crawlid="+crawl.crawlId + "\"><input type=hidden name=review value=\"2\"><input type=submit value=\"Se\"></form>" + "</td>");
			
			buffer.append("</tr>");
		}
		*/
		buffer.append("</table>");
		
		return buffer.toString();
	}
	
}
