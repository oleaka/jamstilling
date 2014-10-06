package no.jamstilling.jettyserver.parser;

import java.io.IOException;
import java.io.InputStream;

public class CrawlManager {

	public boolean startCrawl(String domain) {
		
		String command = "java -jar crawler.jar no.jamstilling.crawler.Crawler";
		
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
