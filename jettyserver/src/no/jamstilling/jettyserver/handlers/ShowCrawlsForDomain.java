package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

	private String cleanUrl(String url) {
		
		if(!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "http://" + url;
		}
		if(url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		return url;
	}

	
	private String getDomain(String url) throws MalformedURLException {
		URL myUrl = new URL(cleanUrl(url));
		String host = myUrl.getHost();
		if(host.startsWith("www.")) {
			return host.substring(4);
		}
		return host;

	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
    	if(urlToHandle.equals(target)) {
    		
    		String domain = request.getParameter("domain");
    		
    		response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	
    		try {
        		System.out.println("arg: " + domain);
    			domain = getDomain(domain);
        		System.out.println("using: " + domain);
	
		        StorageHandler storage = new StorageHandler();
		        storage.connect(domain);
	
		        List<Crawl> inProgressCrawls = storage.getUnfinishedCrawls();
		        List<Crawl> finishedCrawls = storage.getFinishedCrawls();
		        
		        String inProgressText = createInProgressText(inProgressCrawls, domain);
		        String finishedText = createFinishedText(finishedCrawls, domain);
		        
		        String fileContent = getFileContent();
	
		        fileContent = fileContent.replaceAll("%DOMAIN%", domain);
		        fileContent = fileContent.replace("%FINISHED_TABLE%", finishedText);
		        fileContent = fileContent.replace("%UNFINISHED_TABLE%", inProgressText);
	    	
		        response.getWriter().println(fileContent);
    		} catch (Exception e) {
    			response.getWriter().println(e.getMessage());
    		}
    	}
    }
	
	private String createInProgressText(List<Crawl> list, String domain) {
		if(list.size() > 0) {
			return createTable(list, domain);
		} else {
			return createStartCrawlText(domain);
		}
	}
	
	private String createFinishedText(List<Crawl> list, String domain) {
		if(list.size() > 0) {
			return createTable(list, domain);
		} else {
			return "Ingen ferdige undersøkelser";
		}
	}
	
	private String createTable(List<Crawl> list, String domain) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<table border=\"1\">");

		buffer.append("<tr>");
	    buffer.append("<th>Id</th>");
	    buffer.append("<th>Startet</th>");
	    buffer.append("<th>Ferdig</th>");
	    buffer.append("<th>Resultat</th>");
		buffer.append("</tr>");
		
		for(Crawl crawl : list) {
			buffer.append("<tr>");
			
			buffer.append("<td>" + crawl.crawlId + "</td>");
			buffer.append("<td>" + crawl.started + "</td>");
			buffer.append("<td>" + crawl.ended + "</td>");
			buffer.append("<td>" + "<form method=POST action=\"result?domain="+domain+"&crawlid="+crawl.crawlId +"&filter=&level=2\"><input type=hidden name=review value=\"2\"><input type=submit value=\"Se\"></form>" + "</td>");
			
			buffer.append("</tr>");
		}
		buffer.append("</table>");
		
		return buffer.toString();
	}

	private String createStartCrawlText(String domain) {
		return "Ingen aktive undersøkelser.<br>"
			+ "<form method=POST action=\"crawl_start?domain="+domain+ "\"><input type=hidden name=review value=\"2\"><input type=submit value=\"Start ny undersøkelse\"></form>";
	}
}
