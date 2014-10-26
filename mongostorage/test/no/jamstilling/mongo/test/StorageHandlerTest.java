package no.jamstilling.mongo.test;

import java.net.UnknownHostException;

import junit.framework.Assert;

import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.result.CrawlResult;

import org.junit.Test;

public class StorageHandlerTest {

	@Test
	public void testInsert() {
		
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("testinserts");
			storage.newCrawl();
			
			String urlToInsert = "http://www.testinserts.com";
			storage.insertUnparsedPage(urlToInsert);
			
//			String url = storage.getNextLink();
//			Assert.assertEquals(urlToInsert, url);
	
			storage.crawlDone();
			
		} catch (Exception e) {
			System.err.println("failed");
			e.printStackTrace();
		}
	}

	
	@Test
	public void testResult() {
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("testinserts");
			storage.newCrawl();
			
			storage.insertPageResult("http://www.testinserts.com", "bla bla", 2147483600, 2147483600, 8, 0);
			storage.insertPageResult("http://www.testinserts.com/eple", "bla bla bla", 2147483600, 10, 8, 2);
			
			storage.crawlDone();
			
			CrawlResult res = storage.getResult("");
			
			System.out.println(res);
			
		} catch (Exception e) {
			System.err.println("failed");
			e.printStackTrace();
		}
	}
	
	@Test
	public void testParticularResult() {
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("janneeikeland.blogg.no");
			storage.continueCrawl("1");
				
			CrawlResult res = storage.getResult("");
			
			System.out.println(res);
	
		} catch (Exception e) {
			System.err.println("failed");
			e.printStackTrace();
		}
	
	}
}

