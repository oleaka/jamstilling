package no.jamstilling.crawler.language;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import no.jamstilling.crawler.DomeneParser;
import no.jamstilling.crawler.ParseResult;
import no.jamstilling.crawler.WebCreator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LanguageAnalyzer {
	
	static DecimalFormat twoDForm = new DecimalFormat("#.##");
      
	public static String[] nynorskOrd = {"ein", "korleis", "ikkje", "eg", "eit", "ho", "frå", "blei", "berre", "no", "noko", "vere", "nokon", "sjølv", "vore", "gjekk", "meir", "kva", "fekk", "heile", "saman", "fleire", "mykje"};	
	public static String[] bokmaalOrd = {"en", "hvordan", "ikke", "jeg", "et", "hun", "fra", "vart", "bare", "nå", "noe", "være", "noen", "selv", "vært", "gikk", "mer", "hva", "fikk", "hele", "sammen", "flere", "mye"};
	public static String[] engelskOrd = {"one", "they", "not", "she", "from", "only", "some", "now", "self", "said", "walk", "walked", "more", "where", "together", "whole"};
	
	public static ParseResult analyzeBody(String body, String url) {
		ParseResult res = analyzeTextBlock(url, body);		
		ArrayList<String> text = new ArrayList<String>();
		text.add(body);
		report(res, url, text);
		return new ParseResult(/*url,*/ res);
	}
	
	public static ParseResult analyzeBody(Document doc, String url) {

		ArrayList<String> foundText = new ArrayList<String>();
		Elements elem = doc.body().children();
		
		for(Element e: elem) {
			parseElement(e, foundText);
		}
		
		String body = extract(foundText);

		ParseResult res = analyzeTextBlock(url, body);
		
		report(res, url, foundText);
		
		return res;
	//	return new ParseResult(/*url, */res);
	}	

	private static ParseResult analyzeTextBlock(String url, String body) {
		String[] split = body.split(" ");
		
		int nynorsk = occurenceCount(split, nynorskOrd);
		int bokmaal = occurenceCount(split, bokmaalOrd);
		int english = occurenceCount(split, engelskOrd);
		
		return new ParseResult(url, body, split.length,  nynorsk, bokmaal, english);	
	}
		
	private static void report(ParseResult res, String url, ArrayList<String> text) {
		
		DomeneParser.display.setText(url + " - nn: " +  String.format("%.2f", res.getNynorskProsent()*100.0) + "% bm:" + String.format("%.2f", res.getBokmaalProsent()*100.0) +"%");
		try {
			String lokalFil = WebCreator.createPage(res, url, text, res.getHits(LanguageDefinitions.NYNORSK), res.getWords(LanguageDefinitions.NYNORSK), 
					res.getHits(LanguageDefinitions.BOKMAAL), res.getWords(LanguageDefinitions.BOKMAAL), res.getHits(LanguageDefinitions.ENGLISH), res.getWords(LanguageDefinitions.ENGLISH));
			res.setLocalLink(lokalFil);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	private static String extract(ArrayList<String> foundText) {
		
		StringBuffer buffer = new StringBuffer();
		for(String part : foundText) {
			buffer.append(part);
			buffer.append(" ");
		}
		return buffer.toString();
	}
	
	private static boolean isRealText(String text) {
		if(text == null || text.length() < 50) {
			return false;
		}
		return true;
	}
	
	private static void parseElement(Element elem, ArrayList<String> foundText) {
		Elements elements = elem.children();
		for(Element e : elements) {
			parseElement(e,  foundText);
		}
		if(elements.size() == 0 && elem.hasText()) {
			String text = elem.text();
			if(isRealText(text)) {
				foundText.add(text);
			}
		}
	}

	static double roundTwoDecimals(double d) {
        return Double.valueOf(twoDForm.format(d));
	}

	
	private static int occurenceCount(String[] words, String[] lookFor) {
		if(words == null || words.length == 0 || lookFor == null || lookFor.length == 0) {
			return 0;
		} 

		int counter = 0;
		for(int i = 0; i < words.length; i++) {
			for(int j = 0; j < lookFor.length; j++) {
				if(words[i].equalsIgnoreCase(lookFor[j])) {
					counter++;
				}
			}
		}
		return counter;
	}
}
