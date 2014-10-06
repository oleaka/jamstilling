package no.jamstilling.jettyserver.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class CrawlManager {

	private String cleanUrl(String url) {
		
		if(!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "http://" + url;
		}
		if(url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		return url;
	}

	
	public boolean startCrawl(String domain) throws MalformedURLException, URISyntaxException {
		
		URL url = new URL(cleanUrl(domain));
		
		String command = "java -jar crawler.jar no.jamstilling.crawler.Crawler \"" + url.toURI().toString() + "\"";
		
		try {
			Process process = Runtime.getRuntime().exec( command );

			InputStream stream = process.getErrorStream();
			int b = stream.read();
			while(b != -1) {
				System.out.print((char)b);
				b = stream.read();
			}
			
			System.out.println("");
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
}
