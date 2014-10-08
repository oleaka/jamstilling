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

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawl extends Thread {
	
	private StorageHandler storage;
	
//	private final HashSet<String> links = new HashSet<String>();
//	private final HashSet<String> parsedLinks = new HashSet<String>();
	
	private final Domene domene;
	private final ArrayList<String> exceptions;
	private final long sleepTime;
	
	public Crawl(Domene domene, String exceptionList, long sleepTime) throws IOException {
		this.domene = domene;
		this.sleepTime = sleepTime;
		this.storage = new StorageHandler();
		this.storage.connect(domene.getDomainPart());
		
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
		
	//	WebCreator.initialize(path + File.separator + domene.getDomainPart());
	//	ResultFileWriter.initialize(path + File.separator + domene.getDomainPart());
	}
	
	public void run() {
		try {
			setupTrust();
		} catch (KeyManagementException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		
		try {			
			addInitialLinks();
			performCrawl();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		
			/*
			try {
				ResultFileWriter.finish();
				CrawlResultSummary res = reportResult();
				DomeneParser.display.setText("\n\n");
				
				double nnProsent = ((double)res.nynorskWords / (double)(res.bokmaalWords + res.nynorskWords))*100.0;
				double bmProsent = ((double)res.bokmaalWords / (double)(res.bokmaalWords + res.nynorskWords))*100.0;
				
				DomeneParser.display.setText("Nynorsk %: " +  String.format("%.2f", nnProsent));
				DomeneParser.display.setText("Bokmål %: " + String.format("%.2f", bmProsent));
		
				DomeneParser.display.setText("Oppsummering: " + WebCreator.getSummaryFile(false));

				WebCreator.createSummary(res, ResultFileWriter.getAdvancedResultSummary(), false);
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				URI summary = new File(WebCreator.getSummaryFile(false)).toURI();			
				Desktop.getDesktop().browse(summary);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			*/
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

	/*
	private CrawlResultSummary reportResult() throws IOException {
		return ResultFileWriter.getGlobalResult(domene.getDomainPart());
	}
	*/
	
	private void addInitialLinks() {
		storage.insertUnparsedPage(domene.getUrl());
	}
	
	private void addLink(String url) {
		storage.insertUnparsedPage(domene.getUrl());
	}
	
	private String getNextLink() {
		return storage.getNextLink();
	}
	
	private void performCrawl() {
		String currentLink = getNextLink();
		if(currentLink != null) {

			try {
				parseURL(currentLink);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	
		CrawlThread t1 = new CrawlThread(1);
		CrawlThread t2 = new CrawlThread(2);
		CrawlThread t3 = new CrawlThread(3);
		CrawlThread t4 = new CrawlThread(4);
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		
		while(!(t1.isDone() && t2.isDone() && t3.isDone() && t4.isDone())) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
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
				System.out.println("Crawl " + num + " done");
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

			if(verifyLink(absoluteLink)) {
				if(!absoluteLink.equals(url)) {
					addLink(absoluteLink);
				} 
			}	

		}

	}
	
	private void parseURL(String url) {
		try {
			System.out.println("parse: " + url);
			Connection conn = Jsoup.connect(url);
			conn.timeout(10000);
			Document doc = conn.get();		
			
			analyzeBody(doc, url);
			findLinks(doc, url);
		} catch (UnsupportedMimeTypeException e) {

			String type = e.getMimeType();
			System.out.println("type: " + type);
			if (type != null) {
				try {
					if (type.toLowerCase().contains("pdf")) {
						System.out.println("pdf");
						String body = DownloadPDF.getBody(url);
						if (body != null) {
							analyzeBody(body, url);
						}
					} else {
						storage.insertPageFailed(url, e.getMessage());
					}
				} catch (Exception x) {
					storage.insertPageFailed(url, x.getMessage());
				} catch (Throwable x) {
					storage.insertPageFailed(url, x.getMessage());
				}
			} else {
				storage.insertPageFailed(url, e.getMessage());
			}
		} catch (HttpStatusException e) {
			storage.insertPageFailed(url, e.getMessage());
		} catch (IOException e) {
			storage.insertPageFailed(url, e.getMessage());
		} catch (Exception e) {
			storage.insertPageFailed(url, e.getMessage());
		} catch (Throwable e) {
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
