package no.jamstilling.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Util {

	public static String unsafe(String arg) {
		try {
			return URLDecoder.decode(arg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return arg;
		}
	}
	
	public static String safe(String arg) {
		try {
			return URLEncoder.encode(arg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return arg;
		}
	}
	
/*
	Properties properties = new Properties();
InputStream inputStream = new FileInputStream("path/to/file");
try {
    Reader reader = new InputStreamReader(inputStream, "UTF-8");
    try {
        properties.load(reader);
    } finally {
        reader.close();
    }
} finally {
   inputStream.close();
}
	*/
	
	public static Map<String, List<String>> getDefaultWords() {
		Properties prop = new Properties();
		
		Map<String, List<String>> words = new HashMap<String, List<String>>();
		try {
			InputStream inputStream = new FileInputStream("words.properties");
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			   
			prop.load(reader);

	 		List<String> nnWords = new LinkedList<String>();
	 		String nn = prop.getProperty("nn");
	 		String[] split = nn.split(",");
	 		for(String word : split) {
	 			word = word.trim();
	 			if(!"".equals(word)) {
	 				nnWords.add(word);
	 			}
	 		}
	 		
	 		List<String> bmWords = new LinkedList<String>();
	 		String bm = prop.getProperty("bm");
	 		split = bm.split(",");
	 		for(String word : split) {
	 			word = word.trim();
	 			if(!"".equals(word)) {
	 				bmWords.add(word);
	 			}
	 		}
	 		
	 		List<String> enWords = new LinkedList<String>();
	 		String en = prop.getProperty("bm");
	 		split = en.split(",");
	 		for(String word : split) {
	 			word = word.trim();
	 			if(!"".equals(word)) {
	 				enWords.add(word);
	 			}
	 		}
	 		
	 		words.put("nn", nnWords);
	 		words.put("bm", bmWords);
	 		words.put("en", enWords);
	 		
	 		
	 		inputStream.close();
	 		reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return words;
	}
	
}
