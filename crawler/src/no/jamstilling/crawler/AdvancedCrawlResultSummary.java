package no.jamstilling.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import no.jamstilling.crawler.language.LanguageDefinitions;
import no.jamstilling.crawler.utils.Utils;

public class AdvancedCrawlResultSummary {

	private HashMap<String, HashMap<String, ParseResult>> result = new HashMap<String, HashMap<String, ParseResult>>();
	
	public AdvancedCrawlResultSummary(HashMap<String, ParseResult> parsedURLS) {
		for(Entry<String, ParseResult> entry : parsedURLS.entrySet()) {
			
			String subPart = Utils.getSubPart(entry.getKey(), 4);
			
			if(!result.containsKey(subPart)) {
				result.put(subPart, new HashMap<String, ParseResult>());
			}
			
			HashMap<String, ParseResult> map = result.get(subPart);
			map.put(entry.getKey(), entry.getValue());
		}
	}
		
	public String getResultForKey(String key) {
		StringBuffer page = new StringBuffer("");

		page.append("<html><body>");
		
		page.append("<table border=\"1\">");

		page.append("<tr>");
		page.append("<td>Ord</td>");
		page.append("<td>Nynorsk</td>");
		page.append("<td>Bokmål</td>");
		page.append("<td>Engelsk</td>");
		page.append("<td>Detaljar</td>");
		page.append("<td>URL</td>");
		page.append("</tr>");
		
		page.append("<p>");
		
		HashMap<String, ParseResult> keyRes = result.get(key);

		
		ArrayList<Entry<String, ParseResult>> list = new ArrayList<Entry<String,ParseResult>>(keyRes.entrySet());
		Collections.sort(list, new Comparator<Entry<String, ParseResult>>() {
			@Override
			public int compare(Entry<String, ParseResult> o1, Entry<String, ParseResult> o2) {
				return o2.getValue().totaltAntallOrd - o1.getValue().totaltAntallOrd;
			}});

		/*
		ArrayList<ParseResult> list = new ArrayList<ParseResult>(keyRes.values());
		Collections.sort(list, new Comparator<ParseResult>() {
			@Override
			public int compare(ParseResult o1, ParseResult o2) {
				return o2.totaltAntallOrd - o1.totaltAntallOrd;
			}});
		*/

		for(Entry<String, ParseResult> res : list) {
			page.append("<tr>");
			page.append("<td>" + Utils.getFormattedNumber(res.getValue().totaltAntallOrd) + "</td>");
			page.append("<td>" + String.format("%.2f", res.getValue().getNynorskProsent()*100.0) + " %</td>");
			page.append("<td>" + String.format("%.2f", res.getValue().getBokmaalProsent()*100.0) + " %</td>");
			page.append("<td>" + String.format("%.2f", res.getValue().getEngelskProsent()*100.0) + " %</td>");
			page.append("<td><a href=\"file:///"+ WebCreator.ROOT_CATALOG + File.separator + res.getValue().getLocalLink() + "\">Detaljar</a></td>");
			page.append("<td>" + res.getKey() + "</td>");
			page.append("</tr>");

		}
		
		page.append("</table>");

		page.append("</html>");

		return page.toString();
	}
	
	public String getComputedResult() {

		StringBuffer page = new StringBuffer("");
		
		page.append("<table border=\"1\">");

		page.append("<tr>");
		page.append("<td>Del</td>");
		page.append("<td>Sider</td>");
		page.append("<td>Nynorsk</td>");
		page.append("<td>Bokmål</td>");
		page.append("<td>Engelsk</td>");
		page.append("<td>Detaljar</td>");
		page.append("</tr>");

		ArrayList<String> keys = new ArrayList<String>(result.keySet());
		Collections.sort(keys);
		
		for(String key : keys) {

			String detailsForKey = getResultForKey(key);
			File detailsFile = new File(WebCreator.ROOT_CATALOG + File.separator + WebCreator.DETAILS_CATALOG + File.separator + System.currentTimeMillis() + ".html");
			
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(detailsFile));
				writer.write (detailsForKey);

				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			HashMap<String, ParseResult> map = result.get(key);
			
			long nynorskOrd = Utils.getNumberOfWords(map, LanguageDefinitions.NYNORSK);
			long bokmaalOrd = Utils.getNumberOfWords(map, LanguageDefinitions.BOKMAAL);
			long englishOrd = Utils.getNumberOfWords(map, LanguageDefinitions.ENGLISH);
	//		long unknownOrd = Utils.getNumberOfWords(map, LanguageDefinitions.UNKNWON);
					
			CrawlResultSummary summary = new CrawlResultSummary(key, map.size(), /*unknownOrd,*/ nynorskOrd, bokmaalOrd, englishOrd);
			
			page.append("<tr>");
			page.append("<td>" + key + "</td>");
			page.append("<td>" + Utils.getFormattedNumber(summary.numberOfUrls) + "</td>");
			page.append("<td>" + String.format("%.2f", Utils.computePersent(summary.nynorskWords, summary.bokmaalWords + summary.nynorskWords + summary.englishWords)*100.0) + " %</td>");
			page.append("<td>" + String.format("%.2f", Utils.computePersent(summary.bokmaalWords, summary.bokmaalWords + summary.nynorskWords + summary.englishWords)*100.0) + " %</td>");
			page.append("<td>" + String.format("%.2f", Utils.computePersent(summary.englishWords, summary.bokmaalWords + summary.nynorskWords + summary.englishWords)*100.0) + " %</td>");
			page.append("<td><a href=\"file:///"+ detailsFile.getAbsolutePath() + "\">Detaljar</a></td>");
			page.append("</tr>");
						
		}
		
		page.append("</table>");
		
		return page.toString();
	}
	
}
