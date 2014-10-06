package no.jamstilling.jettyserver.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.jettyserver.utils.FileWatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class PageNotFoundHandler extends FileHandler  {

	static final Logger logger = LogManager.getLogger(PageNotFoundHandler.class.getName());
	
	public PageNotFoundHandler(String fileName) {
		super(fileName);
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	
		String content = getFileContent();
		
		content = content.replaceAll("%URL_NOT_FOUND%", target);
		
		response.getWriter().println(content);
	} 
	
}
