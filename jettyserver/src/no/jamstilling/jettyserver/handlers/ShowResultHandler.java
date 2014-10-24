package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.result.Crawl;
import no.jamstilling.mongo.result.CrawlResult;
import no.jamstilling.mongo.result.PartialCrawlResult;

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

		        CrawlResult summaryResult = storage.getResult();
		        String summaryTable = createSummaryTable(summaryResult);

		        List<PartialCrawlResult> detailResult = storage.getDetailResult(2);				
		        String detailsTable = createDetailsTable(detailResult);
		        
		        String fileContent = getFileContent();
		        fileContent = fileContent.replaceAll("%DOMAIN%", domain);
		        fileContent = fileContent.replace("%SUMMARY_TABLE%", summaryTable);
		        fileContent = fileContent.replace("%DETAILS_TABLE%", detailsTable);
		        
		        response.getWriter().println(fileContent);
	        } catch (Exception e) {
	        	e.printStackTrace();
		        response.getWriter().println(e.getMessage());	        	
	        }
    	}
    	
	}
	
	private String createDetailsTable(List<PartialCrawlResult> detailResult) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<table border=\"1\">");
		
		buffer.append("<tr>");
	    buffer.append("<th>Del av domene</th>");
	    buffer.append("<th>Antall sider</th>");
	    buffer.append("<th>Nynorsk</th>");
	    buffer.append("<th>Bokmål</th>");
		buffer.append("</tr>");

		
		for(PartialCrawlResult res : detailResult) {
			buffer.append("<tr>");
			buffer.append("<td>" + res.url + "</td>");
			buffer.append("<td>" + res.totalPages + "</td>");
			buffer.append("<td>" + String.format( "%.2f", (((double) res.totalNNWords / (res.totalNNWords+res.totalBMWords))) * 100.0)+ "%</td>");
			buffer.append("<td>" + String.format( "%.2f", (((double) res.totalBMWords / (res.totalNNWords+res.totalBMWords))) * 100.0) + "%</td>");
			buffer.append("</tr>");
		}		
		
		return buffer.toString();
	}
	
	private String createSummaryTable(CrawlResult result) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<table border=\"1\">");
		
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
