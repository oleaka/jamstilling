package no.jamstilling.mongo.test;

import java.net.UnknownHostException;
import java.util.List;

import junit.framework.Assert;

import no.jamstilling.mongo.StorageHandler;
import no.jamstilling.mongo.Util;
import no.jamstilling.mongo.result.CrawlResult;

import org.junit.Test;
import no.jamstilling.mongo.result.PartialCrawlResult;


public class StorageHandlerTest {

	@Test
	public void testInsert() {
		
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("testinserts");
			storage.newCrawl(Util.getDefaultWords());
			
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
	public void testFlow() {
		
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("testflow.no");
			storage.newCrawl(Util.getDefaultWords());
			
			String urlToInsert = "http://www.testflow.no";
			storage.insertUnparsedPage(urlToInsert);

			String gotLink = storage.getNextLink();
			Assert.assertEquals(gotLink,  urlToInsert);
			
			String unparsed1 = "http://www.testflow.no/bla1.html";
			String unparsed2 = "http://www.testflow.no/bla2.html";
			
			storage.insertUnparsedPage(unparsed1);
			storage.insertUnparsedPage(unparsed2);
			
			String link1 = storage.getNextLink();
			String link2 = storage.getNextLink();
			String link3 = storage.getNextLink();
			
			System.out.println("l1: " + link1);
			System.out.println("l2: " + link2);
			System.out.println("l3: " + link3);
			
			storage.insertPageResult(link1, "bla1", 1, 1, 0, 0);
			storage.insertPageResult(link2, "bla2", 1, 1, 0, 0);
			
			
			storage.crawlDone();
	//		storage.insertUnparsedPage(url);
			
			
			
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
			storage.newCrawl(Util.getDefaultWords());
			
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
	
	@Test
	public void testGetDetailsResult() {
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("test_details_result_db");
			storage.newCrawl(Util.getDefaultWords());
			
			storage.insertPageResult("http://www.smp.no", "bla bla", 20, 2, 10, 2);
			storage.insertPageResult("http://www.smp.no/test/bla1.html?bla1=bla1&bla2=bla2", "bla bla", 20, 2, 10, 2);
			storage.insertPageResult("http://www.smp.no/test/bla2.html?eple=kake&banan=kake", "bla bla", 20, 2, 10, 2);
			storage.insertPageResult("http://www.smp.no/test/mat/bla1.html?bla1=bla1&bla2=bla2", "bla bla", 20, 2, 10, 2);
			storage.insertPageResult("http://www.smp.no/test/mat/bla2.html?eple=kake&banan=kake", "bla bla", 20, 2, 10, 2);
			
			List<PartialCrawlResult> resList = storage.getDetailResult(3, "www.smp.no/test");
			
			for(PartialCrawlResult pRes : resList) {
				System.out.println(pRes.toString());
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
			
	
	@Test
	public void testGetResults() {
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("test_result_db");
			storage.newCrawl(Util.getDefaultWords());
			
			storage.insertPageResult("http://www.smp.no", "bla bla", 20, 2, 10, 2);
			storage.insertPageResult("http://www.smp.no/test/bla1.html?bla1=bla1&bla2=bla2", "bla bla", 20, 2, 10, 2);
			storage.insertPageResult("http://www.smp.no/test/bla2.html?eple=kake&banan=kake", "bla bla", 20, 2, 10, 2);
			
			CrawlResult unfilteredresult = storage.getResult("");
			
			CrawlResult filteredresult = storage.getResult("http://www.smp.no/test/");

			System.out.println("unfiltered");
			System.out.println(unfilteredresult.toString());
			System.out.println("filtered");
			System.out.println(filteredresult.toString());
			
			Assert.assertEquals(3, unfilteredresult.totalPages);
			Assert.assertEquals(2, filteredresult.totalPages);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
}

