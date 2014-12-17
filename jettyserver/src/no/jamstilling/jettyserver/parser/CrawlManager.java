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

	
	public void startCrawl(String domain) throws Exception {
		
		URL url = new URL(cleanUrl(domain));
		
	//	String command = "java -jar crawler.jar no.jamstilling.crawler.Crawler \"" + url.toURI().toString() + "\"";
		
		String command = "java -jar crawler.jar \"" + url.toURI().toString() + "\"";
		Process proc = Runtime.getRuntime().exec( command );
		
		// handle process' stdout stream
		Thread out = new StreamHandlerThread( proc.getInputStream() );
		out.start();

		// handle process' stderr stream
		Thread err = new StreamHandlerThread( proc.getErrorStream() );
		err.start();
		
		/*
		InputStream stream = process.getErrorStream();
		int b = stream.read();
		StringBuffer buffer = new StringBuffer();
		while(b != -1) {
			System.out.print((char)b);
			b = stream.read();
			buffer.append((char)b);
		}
		
		System.out.println("");
		
		if(buffer.length() > 0) {
			throw new Exception(buffer.toString());
		}
		*/
	}
	
	class StreamHandlerThread extends Thread {
		public InputStream stream;
		public StreamHandlerThread(InputStream inputStream) {
			this.stream = inputStream;
		}
		
		public void run() {
			try {
				int b = stream.read();
				while(b != -1) {
					b = stream.read();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
