package no.jamstilling.crawler.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;

import no.jamstilling.crawler.ParseResult;

public class Utils {

	public static long getNumberOfWords(HashMap<String, ParseResult> parsedURLS, int langType) {
		long counter = 0;
		for(ParseResult result : parsedURLS.values()) {
			counter += result.getWords(langType);
		}
		return counter;
	}
	
	public static String getSubPart(String url, int maxParts) {
		if(url.startsWith("http://")) {
			url = url.substring(7);
		} else if(url.startsWith("https://")) {
			url = url.substring(8);
		}
		
		int parts = url.length() - url.replace("/", "").length();
		if(parts > maxParts) {
			int index = url.lastIndexOf("/");
			if(index > 0) {
				index = url.substring(0, index).lastIndexOf("/");
				if(index > 0) {
					return url.substring(0, index);			
				}
			}
		}
		
		int index = url.lastIndexOf("/");
		if(index > 0) {
			return url.substring(0, index);
		}
		return url;
	}
	
	
	public static String getFormattedNumber(int number) {

		String asString = Integer.toString(number);
		if(number < 1000) {
			return asString;
		} else {
			return asString.substring(0, asString.length()-3) + " " + asString.substring(asString.length()-3);
		}		
	}

	public static double computePersent(long words, long totalWords) {
		if(totalWords == 0) {
			return 0.0;
		} else {
			return (double)words / (double)totalWords;
		}
	}

}
