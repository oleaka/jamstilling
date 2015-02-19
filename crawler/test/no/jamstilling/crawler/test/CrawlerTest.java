package no.jamstilling.crawler.test;

import java.io.IOException;
import java.util.List;

import no.jamstilling.crawler.Crawl;
import no.jamstilling.crawler.DomeneParser;
import no.jamstilling.crawler.domene.Domene;
import no.jamstilling.crawler.download.DownloadPDF;
import no.jamstilling.crawler.language.LanguageAnalyzer;

import org.junit.Test;


public class CrawlerTest {

	@Test
	public void testCrawl() {
		
		try {
			Crawl crawl = new Crawl(new Domene("vikebladet.no"), "", 400);	
			
			//Crawl crawl = new Crawl(new Domene("http://www.uio.no"), "", 400);	
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
	
	@Test
	public void testWordSplit() {
		List<String> split = LanguageAnalyzer.splitWords("Dette er en test med en mail adresse, f.eks ole@dole.no. Kvorleis går det.. ");
		for(String w : split) {
			System.out.println(w);
		}
	}
	
}
