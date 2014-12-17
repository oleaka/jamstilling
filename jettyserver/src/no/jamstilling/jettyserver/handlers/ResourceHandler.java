package no.jamstilling.jettyserver.handlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ResourceHandler extends AbstractHandler {

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("ResourceHandler: " + target);
		if(target.startsWith("/resources/")) {
			
			try {

				String content = readFile(target.substring(1));
				
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
			        
				response.getWriter().println(content);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}		
	}
	
	
	
	protected String readFile(String fileName) throws IOException {
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
