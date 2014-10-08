package no.jamstilling.mongo.test;

import java.net.UnknownHostException;

import junit.framework.Assert;

import no.jamstilling.mongo.StorageHandler;

import org.junit.Test;

public class StorageHandlerTest {

	@Test
	public void testInsert() {
		
		StorageHandler storage = new StorageHandler();
		try {
			storage.connect("testinserts");
		
			String urlToInsert = "http://www.testinserts.com";
			storage.insertUnparsedPage(urlToInsert);
			
//			String url = storage.getNextLink();
//			Assert.assertEquals(urlToInsert, url);
	
			storage.crawlDone();
			
		} catch (UnknownHostException e) {
			System.err.println("failed");
			e.printStackTrace();
		}
		
		
		
		
	}
	
}

