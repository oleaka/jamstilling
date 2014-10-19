package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.result.Crawl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

public class ShowCrawlsForDomain extends FileHandler{

static final Logger logger = LogManager.getLogger(ShowCrawlsForDomain.class.getName());
	
	private final String urlToHandle;
	
	public ShowCrawlsForDomain(String urlToHandle, String fileName) {
		super(fileName);
		this.urlToHandle = urlToHandle;
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
    	if(urlToHandle.equals(target)) {
    		
    		String domain = request.getParameter("domain");
    		System.out.println(domain);
    		domain = "tidstankar.no";
    	
    		try {
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);

	        StorageHandler storage = new StorageHandler();
	        storage.connect(domain);

	        List<Crawl> finishedCrawls = storage.getFinishedCrawls();
	        List<Crawl> inProgressCrawls = storage.getUnfinishedCrawls();
	        
	        String finishedCrawlsTable = createTable(finishedCrawls);
	        String inProgressCrawlsTable = createTable(inProgressCrawls);
	        
	        String fileContent = getFileContent();

	        fileContent = fileContent.replaceAll("%DOMAIN%", domain);
	        fileContent = fileContent.replace("%FINISHED_TABLE%", finishedCrawlsTable);
	        fileContent = fileContent.replace("%IN_PROGRESS_TABLE%", inProgressCrawlsTable);
    	
	        response.getWriter().println(fileContent);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
	
	private String createTable(List<Crawl> list) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<table>");
		for(Crawl crawl : list) {
			buffer.append("<tr>");
			
			buffer.append("<td>" + crawl.started + "</td>");
			buffer.append("<td>" + crawl.ended + "</td>");
			
			buffer.append("</tr>");
		}
		buffer.append("</table>");
		
		return buffer.toString();
	}
}
