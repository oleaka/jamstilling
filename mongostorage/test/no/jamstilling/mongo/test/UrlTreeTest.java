package no.jamstilling.mongo.test;

import java.util.HashMap;

import no.jamstilling.mongo.result.UrlTree;

import org.junit.Test;

public class UrlTreeTest {

	@Test
	public void testTree() {
		
		String domene = "http://www.vg.no";
		
		String[] urls = {
				"http://www.vg.no",
				"http://www.vg.no/test/jaggu.html",
				"http://www.vg.no/test/teppe.html",
				"http://www.vg.no/eple/kake/topp.html",
				"http://www.vg.no/banan/kake/topp.html",
				"http://www.vg.no/noe.html"				
		};
		
		UrlTree tree = new UrlTree();
		for(String url : urls) {
			tree.addUrl(url);
		}
		
	//	tree.printTree();
	//tree.printSubParts();
		
		 HashMap<String, Integer> urlMap = tree.getParts(2);
		
		for(String url : urlMap.keySet()) {
			System.out.println(url + " => " + urlMap.get(url));
		}
		
	}
	
}
