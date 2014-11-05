package no.jamstilling.crawler.test;

import java.io.IOException;

import no.jamstilling.crawler.Crawl;
import no.jamstilling.crawler.DomeneParser;
import no.jamstilling.crawler.domene.Domene;
import no.jamstilling.crawler.download.DownloadPDF;

import org.junit.Test;


public class CrawlerTest {

	@Test
	public void testCrawl() {
		
		try {
			Crawl crawl = new Crawl(new Domene("http://www.sprakradet.no"), "", 500);	
			crawl.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testPDF() {
		String url = "http://www.sprakradet.no/upload/Klarspr%c3%a5k/Dokumenter/Ver%20klar.pdf";
		try {
			String body = DownloadPDF.getBody(url);
			if(body != null) {
				System.out.println(body.substring(0,  20));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
