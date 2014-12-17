package no.jamstilling.mongo;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
	
	
	private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private static String logFileName = "log.txt";
	public static PrintWriter logFile = null;
	
	
	public static void initializeLog(String fileName) {
		logFileName = fileName;
	}
	
	private static PrintWriter getLogFile() {
		if(logFile == null) {
			try {
				logFile = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true)));
				logFile.println(System.getProperty("java.version"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logFile;
	}
	
	public static void flushLog() {
		PrintWriter log = getLogFile();
		if(log != null){
			synchronized (log) {
				log.flush();				
			}
		}
	}
	
	public static void log(String message) {
		log(message, null);
	}
	
	public static synchronized void log(String message, Exception e) {
		//System.out.println(message);
		PrintWriter log = getLogFile();
		if(log != null){
			synchronized (log) {
				log.println(format.format(new Date()) + ": " + message);
				if(e != null) {
					log.println(e.getMessage());
					for(StackTraceElement elem : e.getStackTrace()) {
						if(elem != null)
							log.println(elem);
					}
				}
				/*
				log.flush();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				*/
				
			}
		}
				
	}
}
