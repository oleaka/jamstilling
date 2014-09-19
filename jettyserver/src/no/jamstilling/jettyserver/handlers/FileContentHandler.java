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

import no.jamstilling.jettyserver.FileWatcher;
import no.jamstilling.jettyserver.JettyServer;

import static java.nio.file.StandardWatchEventKinds.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class FileContentHandler extends AbstractHandler {

	static final Logger logger = LogManager.getLogger(FileContentHandler.class.getName());
	
	private final String fileName;
	private final String urlToHandle;
	
	private String fileContent = "";
	
	public FileContentHandler(String urlToHandle, String fileName) {
		this.fileName = fileName;
		this.urlToHandle = urlToHandle;

		try {
			this.fileContent = readFile();
		} catch (IOException e) {
			logger.error("Error reading file", e);
		}
		try {
			FileWatcher.registerFile(new File(fileName).toPath(), this);
		} catch (IOException e) {
			logger.error("Error registering file", e);
		}
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	
    	if(urlToHandle == null || urlToHandle.equals(target)) {
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        
	        response.getWriter().println(fileContent);
    	} 
	}
	
	public void fileChanged(Path path) {
		try {
			fileContent = readFile();
		} catch (IOException e) {
			logger.error("Error reading file", e);
		}
	}
		
    private String readFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    } 


}
