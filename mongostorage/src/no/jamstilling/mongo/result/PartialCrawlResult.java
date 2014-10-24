package no.jamstilling.mongo.result;

public class PartialCrawlResult {
	public final String url;
	public final int totalPages;
	public final long totalWords;
	public final long totalNNWords;
	public final long totalBMWords;
	public final long totalENWords;
	
	public PartialCrawlResult(String url, int totalPages, long total, long nn, long bm, long en) {
		this.url = url;
		this.totalPages = totalPages;
		this.totalWords = total;
		this.totalNNWords = nn;
		this.totalBMWords = bm;
		this.totalENWords = en;
	}
}
