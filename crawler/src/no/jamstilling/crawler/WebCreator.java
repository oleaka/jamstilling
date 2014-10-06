package no.jamstilling.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import no.jamstilling.crawler.language.LanguageAnalyzer;
import no.jamstilling.crawler.utils.Utils;

public class WebCreator {

	static String ROOT_CATALOG = "";
	static String PAGES_CATALOG = "";
	static String DETAILS_CATALOG = "";
	static final String nyNorskFormat = "<font style=\"BACKGROUND-COLOR: 206BA4\" color=\"white\">####</font>";
	static final String bokmaalFormat = "<font style=\"BACKGROUND-COLOR: 336600\" color=\"white\">####</font>";
	static final String engelskFormat = "<font style=\"BACKGROUND-COLOR: E86850\" color=\"white\">####</font>";
	
	static final SimpleDateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	static final SimpleDateFormat fileTimeFormat = new SimpleDateFormat("HH_mm_ss_");
	
	public static void initialize(String dir) {
		ROOT_CATALOG = dir;

		
		PAGES_CATALOG = "sider";
		DETAILS_CATALOG = "det";
		
		File pagesDir = new File(dir + File.separator + PAGES_CATALOG);
		if(!pagesDir.exists()) {
			pagesDir.mkdirs();
		}
		
		File detailsDir = new File(dir + File.separator + DETAILS_CATALOG);
		if(!detailsDir.exists()) {
			detailsDir.mkdirs();
		}

	}
	
	public static synchronized void error(String msg) {
		File errorFile = new File(ROOT_CATALOG + File.separator + "error.log");
	
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(errorFile, true));
			writer.append(msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}		
	}
	
	private static synchronized void debug(String msg) {
		File debFile = new File(ROOT_CATALOG + File.separator + "debug.log");
	
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(debFile, true));
			writer.append(msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
	}

	
	private static boolean isWord(String word, String[] compareTo) {
		if(word == null || word.equals("")) {
			return false;
		}
		for(String comp : compareTo) {
			if(word.equalsIgnoreCase(comp)) {
				return true;
			}
		}
		return false;
	}
	
	private static String createFormattedWord(String word) {
		if(isWord(word, LanguageAnalyzer.nynorskOrd)) {
			return nyNorskFormat.replace("####", word);
		} else if(isWord(word, LanguageAnalyzer.bokmaalOrd)) {
			return bokmaalFormat.replace("####", word);			
		} else if(isWord(word, LanguageAnalyzer.engelskOrd)) {
			return engelskFormat.replace("####", word);			
		}
		return word;
	}
	
	private static String createPageContent(String url, ArrayList<String> text, int treffNynorsk, int antallNynorsk, int treffBokmaal, int antallBokmaal, int treffEngelsk, int antallEngelsk) {
		
		StringBuffer page = new StringBuffer();
		
		page.append("<html><body>");
		
		page.append("<table border=\"1\">");

		page.append("<tr>");
		page.append("<td>URL</td>");
		page.append("<td><a href=\"" + url + "\">" + url + "</a></td>");
		page.append("</tr>");
		
		page.append("<tr>");
		page.append("<td>Tidspunkt</td>");
		page.append("<td>" + timeFormat.format(Calendar.getInstance().getTime()) + "</td>");
		page.append("</tr>");

		page.append("<tr>");
		page.append("<td>Nynorsk</td>");
		page.append("<td>" + nyNorskFormat.replace("####", treffNynorsk + " => " + antallNynorsk + " ord") + "</td>");
		page.append("</tr>");

		page.append("<tr>");
		page.append("<td>Bokmål</td>");
		page.append("<td>" + bokmaalFormat.replace("####", treffBokmaal + " => " + antallBokmaal + " ord") + "</td>");
		page.append("</tr>");
		
		page.append("<tr>");
		page.append("<td>Engelsk</td>");
		page.append("<td>" + engelskFormat.replace("####", treffEngelsk + " => " + antallEngelsk + " ord") + "</td>");
		page.append("</tr>");

		
		page.append("</table>");
		
		for(String paragraph : text) {
			page.append("<p>");
			String[] split = paragraph.split(" ");
			
			for(String word : split) {
				page.append(createFormattedWord(word) + " ");
			}
			
			page.append("</p>");
		}
		
		
		page.append("</body></html>");
		
		return page.toString();
	}
	
	static synchronized String toFileName(final String url) {
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
		}
		return System.currentTimeMillis() + "";
	}
	
	static String createPage(ParseResult res, String url, ArrayList<String> text, int treffNynorsk, int totAntallNynorsk, int treffBokmaal, int totAntallBokmaal , int treffEngelsk, int totAntallEngelsk) throws IOException {
		
		String content = createPageContent(url, text, treffNynorsk, totAntallNynorsk, treffBokmaal, totAntallBokmaal, treffEngelsk, totAntallEngelsk);
	
		String filename = toFileName(url) + ".html";
		
		File logFile=new File(ROOT_CATALOG + File.separator + PAGES_CATALOG + File.separator + filename);

		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
	    writer.write (content);
	    
	    writer.close();
	    return PAGES_CATALOG + File.separator + filename;
	    // return logFile.getAbsolutePath();
	}

	
	public static void createSummary(CrawlResultSummary result, AdvancedCrawlResultSummary advSummary, boolean temp) {
		StringBuffer page = new StringBuffer();
		
		page.append("<html><body>");
		
		page.append("<table border=\"1\">");

		page.append("<tr>");
		page.append("<td>Domene</td>");
		page.append("<td>" + result.domene + "</td>");
		page.append("</tr>");

		page.append("<tr>");
		page.append("<td>Analyserte sider</td>");
		page.append("<td>" + Utils.getFormattedNumber(result.numberOfUrls) + "</td>");
		page.append("</tr>");

		page.append("<tr>");
		page.append("<td>Tidspunkt</td>");
		page.append("<td>" + timeFormat.format(Calendar.getInstance().getTime()) + "</td>");
		page.append("</tr>");

		page.append("<tr>");
		page.append("<td>Fordeling nynorsk - bokmål</td>");
		page.append("<td>" + String.format("%.2f", Utils.computePersent(result.nynorskWords, result.bokmaalWords + result.nynorskWords)*100.0) + "% - " + String.format("%.2f", Utils.computePersent(result.bokmaalWords, result.bokmaalWords + result.nynorskWords)*100.0) + "%</td>");
		page.append("</tr>");
		
		page.append("<tr>");
		page.append("<td>Nynorsk totalt</td>");
		page.append("<td>" + String.format("%.2f", Utils.computePersent(result.nynorskWords, result.bokmaalWords + result.nynorskWords + result.englishWords)*100.0) + " %</td>");
		page.append("</tr>");

		page.append("<tr>");
		page.append("<td>Bokmål totalt</td>");
		page.append("<td>" + String.format("%.2f", Utils.computePersent(result.bokmaalWords, result.bokmaalWords + result.nynorskWords + result.englishWords)*100.0) + " %</td>");
		page.append("</tr>");
		
		page.append("<tr>");
		page.append("<td>Engelsk totalt</td>");
		page.append("<td>" + String.format("%.2f", Utils.computePersent(result.englishWords, result.bokmaalWords + result.nynorskWords + result.englishWords)*100.0) + " %</td>");
		page.append("</tr>");
		
		page.append("</table>");
		
		page.append("<table border=\"1\">");

		page.append("<p>");

		page.append(advSummary.getComputedResult());

		page.append("<p>");
		
		page.append("</html>");
		
		File logFile=new File(getSummaryFile(temp));

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			writer.write (page.toString());

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static String getSummaryFile(boolean temp) {
		if(!temp) {
			return ROOT_CATALOG + File.separator + "oppsummering.html";
		} else {
			return ROOT_CATALOG + File.separator + fileTimeFormat.format(Calendar.getInstance().getTime()) + "oppsummering.html";
		}
	}
}
