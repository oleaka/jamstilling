package no.jamstilling.crawler;

import java.util.ArrayList;
import java.util.HashMap;

public class CrawlData {

	private final ArrayList<String> links;
	private final HashMap<String, ParseResult> parsedURLS;
	
	@SuppressWarnings("unchecked")
	public CrawlData(ArrayList<String> links, HashMap<String, ParseResult> parsedURLS) {
		this.links = (ArrayList<String>) links.clone();
		this.parsedURLS = (HashMap<String, ParseResult>) parsedURLS.clone();
	}

	public ArrayList<String> getLinks() {
		return links;
	}

	public HashMap<String, ParseResult> getParsedURLS() {
		return parsedURLS;
	}
}
