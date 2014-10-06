package no.jamstilling.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Crawler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new Crawler().test();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}

	private void test() throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter writer = new PrintWriter("crawler.log", "UTF-8");
		writer.println("The first line");
		writer.println("The second line");
		writer.close();
		
	} 
	
}
