package no.jamstilling.jettyserver.handlers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.jamstilling.jettyserver.JettyServer;
import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.Util;
import no.jamstilling.mongo.result.PartialCrawlResult;
import no.jamstilling.mongo.result.SinglePage;

import org.eclipse.jetty.server.Request;

public class ShowResultPageHandler extends FileHandler {

	static final String nyNorskFormat = "<font style=\"BACKGROUND-COLOR: 206BA4\" color=\"white\">####</font>";
	static final String bokmaalFormat = "<font style=\"BACKGROUND-COLOR: 336600\" color=\"white\">####</font>";
	static final String engelskFormat = "<font style=\"BACKGROUND-COLOR: E86850\" color=\"white\">####</font>";
	
	static final DecimalFormat df = new DecimalFormat("#.00");
	//static final Logger logger = LogManager.getLogger(ShowResultPageHandler.class.getName());
			
	private final String urlToHandle;
			
	public ShowResultPageHandler(String urlToHandle, String fileName) {
		super(fileName);
		this.urlToHandle = urlToHandle;
	}
			
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
			
		if(urlToHandle.equals(target)) {
    		String domain = request.getParameter("domain");
    		String crawlId = request.getParameter("crawlid");
    		String url = request.getParameter("url");

    		System.out.println(crawlId + " for " + domain + ", " + url);
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);

	        try {
		        StorageHandler storage = new StorageHandler();
		        storage.connect(domain);
		        storage.continueCrawl(crawlId);
	
		        SinglePage singlePage = storage.getURLContent(url);
		        
		        Map<String, List<String>> wordMap = storage.getWords();
		        String keywordTable = createKeywordTable(wordMap);
		        
		        String summaryTable = createSummaryTable(singlePage, url);
		        String markedContent = createMarkedContent(singlePage);
		        
		        String fileContent = getFileContent();
		        fileContent = fileContent.replaceAll("%URL%", url);
		        fileContent = fileContent.replace("%SUMMARY_TABLE%", summaryTable);
		        fileContent = fileContent.replace("%CONTENT%", markedContent);
		        fileContent = fileContent.replace("%KEYWORDS%", keywordTable);
		        
		        response.getWriter().println(fileContent);
	        } catch (Exception e) {
	        	e.printStackTrace();
		        response.getWriter().println(e.getMessage());	        	
	        }
		}
	}
	
	private String createKeywordTable(Map<String, List<String>> wordMap) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<table border=\"1\">");	

		buffer.append("<tr>");
	    buffer.append("<td>" + JettyServer.getLanguage("NYNORSK") +"</td>");
	    List<String> nn = wordMap.get("nn");
	    buffer.append("<td>" + (nn != null ? nn.toString() : JettyServer.getLanguage("NONE")) +"</td>");
	    buffer.append("</tr>");

	    buffer.append("<tr>");
	    buffer.append("<td>" + JettyServer.getLanguage("BOKMAAL") +"</td>");
	    List<String> bm = wordMap.get("bm");
	    buffer.append("<td>" + (bm != null ? bm.toString() : JettyServer.getLanguage("NONE")) +"</td>");
	    buffer.append("</tr>");
	    
		
		buffer.append("</table>");
		
		return buffer.toString();
	}
	
	private String createSummaryTable(SinglePage page, String url) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<table border=\"1\">");	

		buffer.append("<tr>");
	    buffer.append("<th>" + JettyServer.getLanguage("LANGUAGE_FORM") +"</th>");
	    buffer.append("<th>" + JettyServer.getLanguage("KEYWORDS") +"</th>");
	    buffer.append("<th>" + JettyServer.getLanguage("PERSENT") +"</th>");
	    buffer.append("</tr>");
				
	    buffer.append("<tr>");
	    buffer.append("<td>" + JettyServer.getLanguage("NYNORSK") +"</td>");
	    buffer.append("<td>" + page.wordcountNN +"</td>");
	    buffer.append("<td bgcolor=#206BA4>" + String.format( "%.2f", (((double) page.wordcountNN / Math.max(1,(page.wordcountNN+page.wordcountBM)))) * 100.0)+ "%</td>");
	    buffer.append("</tr>");
	    
	    buffer.append("<tr>");
	    buffer.append("<td>" + JettyServer.getLanguage("BOKMAAL") +"</td>");
	    buffer.append("<td>" + page.wordcountBM +"</td>");
	    buffer.append("<td bgcolor=#336600>" + String.format( "%.2f", (((double) page.wordcountBM / Math.max(1,(page.wordcountNN+page.wordcountBM)))) * 100.0) + "%</td>");
	    buffer.append("</tr>");
	    
	    buffer.append("</table>");
		
		return buffer.toString();
	}
	
	private String createMarkedContent(SinglePage page) {

		StringBuffer buffer = new StringBuffer();
		
		StringBuilder stringBuffer = new StringBuilder(page.content);

		String SWord = "";
		for(int i=0; i<stringBuffer.length(); i++){
			Character charAt = stringBuffer.charAt(i);
			if(Character.isAlphabetic(charAt)){
				SWord = SWord + charAt;
			}
			else{
				if(!SWord.isEmpty()) {
					buffer.append(createFormattedWord(SWord, page.words));
				} 
				buffer.append(charAt);
				SWord = "";
			}	
		}
		
		return buffer.toString();
	}
	
	private static boolean isWord(String word, List<String> compareTo) {
		if(word == null || word.equals("") || compareTo == null) {
			return false;
		}
		for(String comp : compareTo) {
			if(word.equalsIgnoreCase(comp)) {
				return true;
			}
		}
		return false;
	}
	
	private static String createFormattedWord(String word, Map<String, List<String>> words) {
		if(isWord(word, words.get("nn"))) {
			return nyNorskFormat.replace("####", word);
		} else if(isWord(word, words.get("bm"))) {
			return bokmaalFormat.replace("####", word);			
		} else if(isWord(word, words.get("en"))) {
			return engelskFormat.replace("####", word);			
		}
		return word;
	}

}
