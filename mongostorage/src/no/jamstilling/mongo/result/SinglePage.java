package no.jamstilling.mongo.result;

import java.util.List;
import java.util.Map;

public class SinglePage {

	public final String content;
	public final Map<String, List<String>> words;
	public final long wordcount;
	public final long wordcountNN;
	public final long wordcountBM;
	public final long wordcountEN;
	
	
	public SinglePage(String content, Map<String, List<String>> words, long wc, long wcNN, long wcBM, long wcEN) {
		this.content = content;
		this.words = words;
		this.wordcount = wc;
		this.wordcountNN = wcNN;
		this.wordcountBM = wcBM;
		this.wordcountEN = wcEN;
		
	}
}
