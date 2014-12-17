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
import no.jamstilling.mongo.Util;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public abstract class FileHandler extends AbstractHandler  {
	
	private final String fileName;
	private String fileContent = "";
	
	public FileHandler(String fileName) {
		this.fileName = fileName;
		try {
			this.fileContent = readFile();
		} catch (IOException e) {
			Util.log("Error reading file", e);
		}
		try {
			FileWatcher.registerFile(new File(fileName).toPath(), this);
		} catch (IOException e) {
			Util.log("Error registering file", e);
		}
	}

	protected String getFileContent() {
		return fileContent;
	}
	
	public void fileChanged(Path path) {
		try {
			fileContent = readFile();
		} catch (IOException e) {
			Util.log("Error reading file", e);
		}
	}
		

	
    protected String readFile() throws IOException {
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
