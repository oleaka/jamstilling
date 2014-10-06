package no.jamstilling.crawler.utils;

public class GlobalNumberResult {

	public final long total;
	public final long nynorsk;
	public final long bokmaal;
	public final long engelsk;
	
	public GlobalNumberResult(long total, long nynorsk, long bokmaal, long engelsk) {
		this.total = total;
		this.nynorsk = nynorsk;
		this.bokmaal = bokmaal;
		this.engelsk = engelsk;
	}
}
