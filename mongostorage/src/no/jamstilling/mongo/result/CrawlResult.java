package no.jamstilling.mongo.result;

public class CrawlResult {

	public final String domain;
	public final String startTime;
	public final String endTime;	
	public final String crawlId;
	public final int totalPages;
	public final long totalWords;
	public final long totalNNWords;
	public final long totalBMWords;
	public final long totalENWords;
	
	public CrawlResult(String crawlId, String domain, String startTime, String endTime, int totalPages, long total, long nn, long bm, long en) {
		this.crawlId = crawlId;
		this.domain = domain;
		this.startTime = startTime;
		this.endTime = endTime;
		this.totalPages = totalPages;
		this.totalWords = total;
		this.totalNNWords = nn;
		this.totalBMWords = bm;
		this.totalENWords = en;
	}
	
	public String toString() {
		return domain + " (" + startTime + " - " + endTime + "). CrawlId(" + crawlId + "), totalPages(" + totalPages + "), totalWords(" 
				+ totalWords + "), totalNN(" + totalNNWords + "), totalBM(" + totalBMWords + "), totalEN(" + totalENWords + ")";
	}
}
