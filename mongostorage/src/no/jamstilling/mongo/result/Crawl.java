package no.jamstilling.mongo.result;

public class Crawl {

	public final String crawlId;
	public final boolean isFinished;
	public final String started;
	public final String ended;

	public Crawl(String crawlId, boolean isFinished, String started, String ended) {
		this.crawlId = crawlId;
		this.isFinished = isFinished;
		this.started = started;
		this.ended = ended;
	}
}
