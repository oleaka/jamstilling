package no.jamstilling.crawler.language;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import no.jamstilling.crawler.ParseResult;
import no.jamstilling.mongo.result.SinglePage;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LanguageAnalyzer {
	
	static DecimalFormat twoDForm = new DecimalFormat("#.##");
      
	public static String[] nynorskOrd = null;
	public static String[] bokmaalOrd = null;
	public static String[] engelskOrd = null;
	
	public LanguageAnalyzer(Map<String, List<String>> words) {
		List<String> nnWords = words.get("nn");
		nynorskOrd = nnWords.toArray(new String[nnWords.size()]);
		List<String> bmWords = words.get("bm");
		bokmaalOrd = bmWords.toArray(new String[bmWords.size()]);
		List<String> enWords = words.get("en");
		engelskOrd = enWords.toArray(new String[enWords.size()]);
	
	}	
	
	public static ParseResult analyzeBody(String body, String url) {
		ParseResult res = analyzeTextBlock(url, body);		
		ArrayList<String> text = new ArrayList<String>();
		text.add(body);
	//	report(res, url, text);
		return new ParseResult(res);
	}
	
	public static ParseResult analyzeBody(Document doc, String url) {

		ArrayList<String> foundText = new ArrayList<String>();
		Elements elem = doc.body().children();
		
		for(Element e: elem) {
			parseElement(e, foundText);
		}
		
		String body = extract(foundText);

		ParseResult res = analyzeTextBlock(url, body);
		
		return res;
	}	

	private static List<String> splitWords(String text) {

		List<String> words = new LinkedList<String>();
		StringBuilder stringBuffer = new StringBuilder(text);

		String SWord = "";
		for(int i=0; i<stringBuffer.length(); i++){
			Character charAt = stringBuffer.charAt(i);
			if(Character.isAlphabetic(charAt)){
				SWord = SWord + charAt;
			}
			else{
				if(!SWord.isEmpty()) {
					words.add(SWord);
				} 
				SWord = "";
			}	
		}
		
		return words;
	}

	
	private static ParseResult analyzeTextBlock(String url, String body) {
		List<String> split = splitWords(body);
	//	String[] split = body.split(" ");
		
		int nynorsk = occurenceCount(split, nynorskOrd);
		int bokmaal = occurenceCount(split, bokmaalOrd);
		int english = occurenceCount(split, engelskOrd);
		
		return new ParseResult(url, body, split.size(),  nynorsk, bokmaal, english);	
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

	
	private static int occurenceCount(List<String> words, String[] lookFor) {
		if(words == null || words.size() == 0 || lookFor == null || lookFor.length == 0) {
			return 0;
		} 

		int counter = 0;
		for(String word : words) {
			for(String lookForWord : lookFor) {
				if(word.equalsIgnoreCase(lookForWord)) {
					counter++;
				}
			}
		}
		/*		
		for(int i = 0; i < words.size(); i++) {
			for(int j = 0; j < lookFor.length; j++) {
				if(words[i].equalsIgnoreCase(lookFor[j])) {
					counter++;
				}
			}
		}
		*/
		return counter;
	}
}
