package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.Util;
import no.jamstilling.mongo.result.Crawl;
import no.jamstilling.mongo.result.CrawlResult;
import no.jamstilling.mongo.result.PartialCrawlResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

public class ShowResultHandler extends FileHandler {

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
    		String level = request.getParameter("level");
    		if(level == null || "".equals(level)) {
    			level = "2";
    		}
    		String filter = request.getParameter("filter");
    		if(filter == null || "".equals(filter)) {
    			filter = domain;
    		}
    		
    		System.out.println(crawlId + " for " + domain + ", " + exceptions + ", " + level + ", " + filter);
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        
	        try {
		        StorageHandler storage = new StorageHandler();
		        storage.connect(domain);
		        storage.continueCrawl(crawlId);

		        CrawlResult summaryResult = storage.getResult(filter);
		        String summaryTable = createSummaryTable(summaryResult);

		        List<PartialCrawlResult> detailResult = storage.getDetailResult(Integer.parseInt(level), filter);				
		        String detailsSinglePagesTable = createSinglePagesTable(detailResult, domain, crawlId);
		        String detailsSubpartsTable = createSubpartsDetailsTable(detailResult, domain, crawlId, Integer.parseInt(level) + 1);
		        
		        String fileContent = getFileContent();
		        fileContent = fileContent.replaceAll("%DOMAIN%", filter);
		        fileContent = fileContent.replace("%SUMMARY_TABLE%", summaryTable);
		        fileContent = fileContent.replace("%DETAILS_TABLE%", detailsSubpartsTable);
		        fileContent = fileContent.replace("%PAGES_TABLE%", detailsSinglePagesTable);		        
		        
		        response.getWriter().println(fileContent);
	        } catch (Exception e) {
	        	e.printStackTrace();
		        response.getWriter().println(e.getMessage());	        	
	        }
    	}    	
	}

	private String createSinglePagesTable(List<PartialCrawlResult> detailResult, String domain, String crawlId) {
		StringBuffer buffer1 = new StringBuffer();

		buffer1.append("<table border=\"1\">");	
		buffer1.append("<tr>");
	    buffer1.append("<th>URL</th>");
	    buffer1.append("<th>Antall ord</th>");
	    buffer1.append("<th>Nynorsk</th>");
	    buffer1.append("<th>Bokmål</th>");
	    buffer1.append("<th>Detaljer</th>");
	    buffer1.append("</tr>");

	    int counter = 0;
	    
	    for(PartialCrawlResult res : detailResult) {
			if(res.totalPages == 0) {
				counter ++;
				//System.out.println(res);
				buffer1.append("<tr>");
				buffer1.append("<td>" + res.url + "</td>");
				buffer1.append("<td>" + res.totalWords + "</td>");
				buffer1.append("<td>" + String.format( "%.2f", (((double) res.totalNNWords / Math.max(1,(res.totalNNWords+res.totalBMWords)))) * 100.0)+ "%</td>");
				buffer1.append("<td>" + String.format( "%.2f", (((double) res.totalBMWords / Math.max(1, (res.totalNNWords+res.totalBMWords)))) * 100.0) + "%</td>");
				buffer1.append("<td>" + "<form method=POST action=\"resultpage?domain="+domain+"&crawlid="+crawlId + "&url=" + Util.safe(res.url) +"\"><input type=hidden name=review value=\"2\"><input type=submit value=\"Se\"></form>" + "</td>");
				buffer1.append("</tr>");
			}
		}		
	    
	    buffer1.append("</table>");
	    
	    if(counter > 0) {
	    	return buffer1.toString();
	    } else {
	    	return "Ingen";
	    }
	}
	
	private String createSubpartsDetailsTable(List<PartialCrawlResult> detailResult, String domain, String crawlId, int level) {
		StringBuffer buffer2 = new StringBuffer();
		
		buffer2.append("<table border=\"1\">");	
		buffer2.append("<tr>");
	    buffer2.append("<th>URL</th>");
	    buffer2.append("<th>Undersider</th>");
	    buffer2.append("<th>Nynorsk</th>");
	    buffer2.append("<th>Bokmål</th>");
	    buffer2.append("<th>Detaljer</th>");
		buffer2.append("</tr>");
	
		int counter = 0;
		for(PartialCrawlResult res : detailResult) {
			if(res.totalPages > 0) {

				//System.out.println(res);
				counter++;
				buffer2.append("<tr>");
				buffer2.append("<td>" + res.url + "</td>");
				buffer2.append("<td>" + res.totalPages + "</td>");
				buffer2.append("<td>" + String.format( "%.2f", (((double) res.totalNNWords / Math.max(1,(res.totalNNWords+res.totalBMWords)))) * 100.0)+ "%</td>");
				buffer2.append("<td>" + String.format( "%.2f", (((double) res.totalBMWords / Math.max(1,(res.totalNNWords+res.totalBMWords)))) * 100.0) + "%</td>");
				buffer2.append("<td>" + "<form method=POST action=\"result?domain="+domain+"&crawlid="+crawlId + "&filter=" + Util.safe(res.url) + "&level=" + level +"\"><input type=hidden name=review value=\"2\"><input type=submit value=\"Se\"></form>" + "</td>");
				buffer2.append("</tr>");
			}
		}		

	    buffer2.append("</table>");

	    if(counter > 0) {
	    	return buffer2.toString();
	    } else {
	    	return "Ingen";
	    }

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
		String endTime = result.endTime;
		if(endTime == null || "".equals(endTime)) {
			endTime = "Pågår";
		}
		buffer.append("<td>" + endTime + "</td>");
		buffer.append("</tr>");
		
		buffer.append("<tr>");
		buffer.append("<td>Antall sider</td>");
		buffer.append("<td>" + result.totalPages + "</td>");
		buffer.append("</tr>");

		buffer.append("<tr>");
		buffer.append("<td>Nynorsk</td>");
		buffer.append("<td>" + String.format( "%.2f", (((double) result.totalNNWords / Math.max(1,(result.totalNNWords+result.totalBMWords)))) * 100.0)+ "%</td>");
		buffer.append("</tr>");

		buffer.append("<tr>");
		buffer.append("<td>Bokmål</td>");
		buffer.append("<td>" + String.format( "%.2f", (((double) result.totalBMWords / Math.max(1,(result.totalNNWords+result.totalBMWords)))) * 100.0) + "%</td>");
		buffer.append("</tr>");

		
		buffer.append("</table>");
		
		return buffer.toString();
	}
	
}
