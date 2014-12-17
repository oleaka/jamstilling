package no.jamstilling.jettyserver.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.jettyserver.JettyServer;
import no.jamstilling.jettyserver.utils.FileWatcher;

import static java.nio.file.StandardWatchEventKinds.*;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class FileContentHandler extends FileHandler {
	
	private final String urlToHandle;
	
	public FileContentHandler(String urlToHandle, String fileName) {
		super(fileName);
		this.urlToHandle = urlToHandle;
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
    	if(urlToHandle.equals(target)) {
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        
	        response.getWriter().println(getFileContent());
    	} 
	}
	

}
