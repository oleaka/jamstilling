package no.jamstilling.jettyserver.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

public class ShowResultHandler  extends FileHandler {

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
    		
    		System.out.println(domain + ", " + exceptions);
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        
	        String fileContent = getFileContent();
	        fileContent = fileContent.replaceAll("%DOMAIN%", domain);
	        
	        response.getWriter().println(fileContent);

    	}
    	
	}
	
}
