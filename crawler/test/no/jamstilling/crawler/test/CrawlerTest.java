package no.jamstilling.crawler.test;

import java.io.IOException;

import no.jamstilling.crawler.Crawl;
import no.jamstilling.crawler.DomeneParser;
import no.jamstilling.crawler.domene.Domene;

import org.junit.Test;


public class CrawlerTest {

	@Test
	public void testCrawl() {
		
		
		
		try {
			Crawl crawl = new Crawl(new Domene("http://www.tidstankar.no"), "", 200);	
			crawl.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
