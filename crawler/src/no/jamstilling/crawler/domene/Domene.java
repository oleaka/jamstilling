package no.jamstilling.crawler.domene;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;

public class Domene {
	
	private final String url;
	
	private String domainPart;
	
	private Date lastCrawl = null;
	
	public Date getLastCrawl() {
		return lastCrawl;
	}

	public Domene(String link) throws MalformedURLException {
		
		this.url = cleanUrl(link);
		
		this.domainPart = extractDomainPart(url);
		
		System.out.println("Nytt domene : " + url + " => " + domainPart);
	}
	
	
	private String extractDomainPart(String url) throws MalformedURLException {

		URL myUrl = new URL(url);
		String host = myUrl.getHost();
		if(host.startsWith("www.")) {
			return host.substring(4);
		}
		return host;
		
	}
	
	public boolean isPartOfDomain(String url) {
		try {
			return domainPart.equalsIgnoreCase(extractDomainPart(url));
		} catch (MalformedURLException e) {
			return false;
		}		
	}
	
	public String getDomainPart() {
		return domainPart;
	}

	private String cleanUrl(String url) {
	
		if(!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "http://" + url;
		}
		if(url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		return url;
	}
	
	public void setLastCrawl(Date lastCrawl) {
		this.lastCrawl = lastCrawl;
	}
	

	public String getUrl() {
		return url;
	}

	
}
