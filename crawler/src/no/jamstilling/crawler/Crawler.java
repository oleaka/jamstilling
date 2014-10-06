package no.jamstilling.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Crawler {

	static final Logger logger = LogManager.getLogger(Crawler.class.getName());
	
	private int sleepTime = 200;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new Crawler(getDomain(args));
		} catch (IOException e) {
			try {
				log(e);
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} 
	}
	
	private static URL getDomain(String[] args) throws MalformedURLException {
		
		String domain = args[args.length-1];
		URL url = new URL(domain);
		log(domain + " => " + url.toString());
		return url;
	}

	public Crawler(URL domain) throws IOException {
		readConfig();

		String exceptions = "";
		new DomeneParser(domain.toString(), exceptions, sleepTime);
	}
	
	private void readConfig() {
		Properties prop = new Properties();
		
		try {
			InputStream input = new FileInputStream("crawler.properties");
	 		prop.load(input);
	
	 		sleepTime = Integer.parseInt(prop.getProperty("sleep"));
	 		
	 		input.close();
		} catch (IOException ex) {
			logger.error("Error reading config", ex);
		} 
	}

	
	private static void log(Exception e) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(System.currentTimeMillis() + ".log", "UTF-8");

		writer.print(e.getMessage());
		
		writer.close();
		
	}

	private static void log(String msg) {
		try {
			PrintWriter writer = new PrintWriter(System.currentTimeMillis() + ".log", "UTF-8");
			writer.print(msg);
			
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private void test(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter writer = new PrintWriter("crawler.log", "UTF-8");

		
		for(String arg : args) {
			writer.println(arg);			
		}
		
		writer.close();
		
	} 
	
}
