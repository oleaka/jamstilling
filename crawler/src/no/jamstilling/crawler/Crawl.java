package no.jamstilling.crawler;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import no.jamstilling.crawler.domene.Domene;
import no.jamstilling.crawler.download.DownloadPDF;
import no.jamstilling.crawler.language.LanguageAnalyzer;
import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.Util;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawl extends Thread {
	
	private StorageHandler storage;
	
	private final Domene domene;
	private final ArrayList<String> exceptions;
	private final long sleepTime;
	
	private final LanguageAnalyzer analyzer;
	
	public Crawl(Domene domene, String exceptionList, long sleepTime) throws IOException {
		Util.log("Starter crawl med domene: " + domene.getDomainPart(), null);
		this.domene = domene;
		this.sleepTime = sleepTime;

		Map<String, List<String>> words = Util.getDefaultWords();
		this.analyzer = new LanguageAnalyzer(words);
		
		this.storage = new StorageHandler();
		this.storage.connect(domene.getDomainPart());
		this.storage.newCrawl(words);
		
		ArrayList<String> tmpExceptionList = new ArrayList<String>();
		if(exceptionList != null && !"".equals(exceptionList)) {
			String[] asArray = exceptionList.split(",");
			for(String val : asArray) {
				val = val.trim();
				if(!"".equals(val)) {
					tmpExceptionList.add(val);
				}
			}
		}
		this.exceptions = tmpExceptionList;
		
	}
	
	public void run() {
		try {
			Util.log("setting up trust");
			setupTrust();
		} catch (KeyManagementException e1) {
			Util.log("Error setting up trust", e1);
		} catch (NoSuchAlgorithmException e1) {
			Util.log("Error setting up trust", e1);
		}
		
		try {			
			addInitialLinks();
			performCrawl();
		} catch (Exception e) {
			Util.log("Error starting crawl", e);
		} 
	}

	private void setupTrust() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					   	
					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}
					   
					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
			}
		};
		
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		         // Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


	}
	
	private String getInitialURL() {
		String url = domene.getUrl();
	
		try {
			Connection con = Jsoup.connect(url);
			Document doc = con.get();
			if(doc != null) {
				return url;
			}
		} catch (Exception e) {			
		}
		
		return url;
	}
	
	private void addInitialLinks() {
		storage.insertUnparsedPage(getInitialURL());
	}
	
	private void addLink(String url) {
		if(Crawler.debug) {
			Util.log("insertUnparsed: " + url);
		}
		storage.insertUnparsedPage(url);
	}
	
	private String getNextLink() {
		return storage.getNextLink();
	}
	
	private void performCrawl() {
		String currentLink = getNextLink();
		if(currentLink != null) {

			try {
				if(Crawler.debug) {
					Util.log("in performCrawl - calling parseURL");
					Util.flushLog();
				}
				parseURL(currentLink);
				if(Crawler.debug) {
					Util.log("in performCrawl - parseURL done");
					Util.flushLog();
				}
			} catch (Exception e) {
				Util.log("error during initial parse", e);
				Util.flushLog();
				e.printStackTrace();
			} catch (Throwable e) {
				Util.log("error during initial parse: " + e.getMessage());
				Util.flushLog();
				e.printStackTrace();
			}
		}
	
		try {
			if(Crawler.debug) {
				Util.log("Creating crawlthreads");
				Util.flushLog();
			}
			
			CrawlThread t1 = new CrawlThread(1);
			CrawlThread t2 = new CrawlThread(2);
			CrawlThread t3 = new CrawlThread(3);
			CrawlThread t4 = new CrawlThread(4);

			if(Crawler.debug) {
				Util.log("Crawlthreads created");
			}
			
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			
			if(Crawler.debug) {
				Util.log("Crawlthreads started");
			}
			
			while(!(t1.isDone() && t2.isDone() && t3.isDone() && t4.isDone())) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}	
		} catch (Exception e) {
			Util.log("error during startup", e);
		} catch (Throwable e) {
			Util.log("error during startup: " + e.getMessage());
		}
		storage.crawlDone();
	}
	

	class CrawlThread extends Thread {
		
		private int num;
		
		public CrawlThread(int num) {
			super("Crawl " + num);
			this.num = num;
		}
		
		private boolean isDone = false;
		
		public void run() {
			Util.log("crawlthread " + num + " for domain " + domene.getDomainPart() + " starting", null);
			try {
				String currentLink = getNextLink();
				while(currentLink != null) {
	
					try {
						parseURL(currentLink);
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Throwable e) {
						e.printStackTrace();
					}
	
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}
					
					currentLink = getNextLink();
				}
			} finally {
				Util.log("crawlthread " + num + " for domain " + domene.getDomainPart() + " done", null);
				isDone = true;
			}
		}
		
		public boolean isDone() {
			return isDone;
		}
	}
	
	private void findLinks(Document doc, String url) {
		Elements elements = doc.select("a");

		Iterator<Element> it = elements.iterator();
		while(it.hasNext()) {
			Element elem = it.next();
			String absoluteLink = elem.attr("abs:href");
			String localLink = elem.attr("href");
			
			if(localLink.startsWith("#")) {
				continue;
			}

	//		System.out.println("findLinks: " + absoluteLink);
		
			if(Crawler.debug) {
				Util.log("findLinks:: " + absoluteLink);
			}
			
			if(verifyLink(absoluteLink)) {
				if(!absoluteLink.equals(url)) {
					if(Crawler.debug) {
						Util.log("calling addLink: " + absoluteLink);
					}
					addLink(absoluteLink);
					if(Crawler.debug) {
						Util.log("addLink returned: " + absoluteLink);
					}
				} 
			}
		}
		if(Crawler.debug) {
			Util.log("findLinks done");
		}	
	}
	
	private void parseURL(String url) {
		try {
			if(Crawler.debug) {
				Util.log("parse: " + url);
			}
			Connection conn = Jsoup.connect(url);
			conn.timeout(10000);
			Document doc = conn.get();		
	
			if(Crawler.debug) {
				Util.log("body: " + doc);
			}
			
			analyzeBody(doc, url);	
			findLinks(doc, url);
			if(Crawler.debug) {
				Util.log("parse: " + url + " findLinks returned");
			}
		} catch (UnsupportedMimeTypeException e) {

			String type = e.getMimeType();
			if(Crawler.debug) {
				Util.log("type: " + type);
			}

			if (type != null) {
				try {
					if (type.toLowerCase().contains("pdf")) {
						String body = DownloadPDF.getBody(url);
						if (body != null) {
							analyzeBody(body, url);
						}
					} else {
						Util.log("page failed",e);

						storage.insertPageFailed(url, e.getMessage());
					}
				} catch (Exception x) {
					Util.log("page failed",x);

					storage.insertPageFailed(url, x.getMessage());
				} catch (Throwable x) {
					Util.log("page failed: " + x.getMessage());
					storage.insertPageFailed(url, x.getMessage());
				}
			} else {
				Util.log("page failed",e);
				storage.insertPageFailed(url, e.getMessage());
			}
		} catch (HttpStatusException e) {
			Util.log("page failed",e);
			storage.insertPageFailed(url, e.getMessage());
		} catch (IOException e) {
			Util.log("page failed",e);
			storage.insertPageFailed(url, e.getMessage());
		} catch (Exception e) {
			Util.log("page failed",e);
			storage.insertPageFailed(url, e.getMessage());
		} catch (Throwable e) {
			Util.log("page failed: " + e.getMessage());
			storage.insertPageFailed(url, e.getMessage());
		}
	}
	
	
	private boolean isException(String url) {
		if(exceptions == null || "".equals(exceptions)) {
			return false;
		}
		
		for(String exception : exceptions) {
			if(url.toLowerCase().contains(exception.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private boolean verifyLink(String url) {
		if(url != null && url.length() > 0) {			
			String lower = url.toLowerCase();
			if(lower.startsWith("mailto")) {
				return false;
			}
			if(lower.startsWith("mailto")) {
				return false;
			}
			if(lower.endsWith(".doc") || lower.endsWith(".gif") || lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".mov") || lower.endsWith(".avi")  || lower.endsWith(".git")) {
				return false;
			}
 			
			if(isException(url)) {
				return false;
			}
			
			return domene.isPartOfDomain(url);
		} 
		return false;
	}

	
	private void analyzeBody(Document doc, String url) {
		ParseResult result = LanguageAnalyzer.analyzeBody(doc, url);
		storage.insertPageResult(result.url, result.content, result.totaltAntallOrd, result.ordTreffNynorsk, result.ordTreffBokmaal, result.ordTreffEngelsk);
	}

	private void analyzeBody(String body, String url) {
		ParseResult result = LanguageAnalyzer.analyzeBody(body, url);
		storage.insertPageResult(result.url, result.content, result.totaltAntallOrd, result.ordTreffNynorsk, result.ordTreffBokmaal, result.ordTreffEngelsk);
	}

}
