package no.jamstilling.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import no.jamstilling.mongo.Util;

public class Crawler {

	//static final Logger logger = LogManager.getLogger(Crawler.class.getName());
	
	private int sleepTime = 200;
	public static boolean debug = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("starting crawler");
			Util.initializeLog("crawler.log");
			
			new Crawler(getDomain(args));
		} catch (Exception e) {
			Util.log("Error starting crawl", e);
		} 
	}
	
	private static URL getDomain(String[] args) throws MalformedURLException {
		Util.log("args.length: " + args.length);
		for(String arg : args) {
			Util.log("arg: " + arg);
		}
		
		String domain = args[args.length-1];
		Util.log("domain: " + domain);
		if(!domain.startsWith("http://")) {
			domain = "http://" + domain;
		}
		URL url = new URL(domain);
		Util.log(domain + " => " + url.toString(), null);
		return url;
	}

	public Crawler(URL domain) throws IOException {
		readConfig();

		String exceptions = "";
		new DomeneParser(domain.toString(), exceptions, sleepTime);
	}
	
	private void readConfig() {
		Properties prop = new Properties();
		
		try {
			InputStream input = new FileInputStream("crawler.properties");
	 		prop.load(input);
	
	 		sleepTime = Integer.parseInt(prop.getProperty("sleep"));
	 		debug = Boolean.parseBoolean(prop.getProperty("debug"));
	 		input.close();
		} catch (IOException ex) {
			Util.log("Error reading config", ex);
		} 
	}

	
	/*
	private static void log(Exception e) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(System.currentTimeMillis() + ".log", "UTF-8");

		writer.print(e.getMessage());
		
		writer.close();
		
	}

	private static void log(String msg) {
		try {
			PrintWriter writer = new PrintWriter(System.currentTimeMillis() + ".log", "UTF-8");
			writer.print(msg);
			
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	*/

	private void test(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter writer = new PrintWriter("crawler.log", "UTF-8");

		
		for(String arg : args) {
			writer.println(arg);			
		}
		
		writer.close();
		
	} 
	
}
