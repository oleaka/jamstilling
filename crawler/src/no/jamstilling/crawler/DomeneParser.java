package no.jamstilling.crawler;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import no.jamstilling.crawler.domene.Domene;

public class DomeneParser {

	
	private final Crawl crawl;
	
	public DomeneParser(String domene, String exceptions, long sleepTime) throws IOException {
		
		crawl = new Crawl(new Domene(domene), exceptions, sleepTime);	
		crawl.start();
	}
}
