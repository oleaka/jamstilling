package no.jamstilling.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import no.jamstilling.mongo.result.Crawl;
import no.jamstilling.mongo.result.CrawlResult;
import no.jamstilling.mongo.result.PartialCrawlResult;
import no.jamstilling.mongo.result.SinglePage;
import no.jamstilling.mongo.result.UrlTree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class StorageHandler {

	static final String CRAWLID = "crawlid";
	static final String URL = "url";
	static final String STARTED = "started";
	static final String ENDED = "ended";
	static final String DONE = "done";
	
	static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	static final Logger logger = LogManager.getLogger(StorageHandler.class.getName());
	
	private int port = 27017;
	private String host = "localhost";
	
	private String domain = null;
	private MongoClient connection = null;
	private DB dbConnection = null;
	private DBCollection resultCollection = null;
	private DBCollection linkCollection = null;
	private DBCollection errorCollection = null;
	private DBCollection crawlsCollection = null;
	private DBCollection wordsCollection = null;
	
	private String crawlId = null;
	
	public StorageHandler() {
		readConfig();
	}
	
	private void readConfig() {
		Properties prop = new Properties();
		
		try {
			InputStream input = new FileInputStream("mongo.properties");
	 		prop.load(input);
	
	 		port = Integer.parseInt(prop.getProperty("port", "21017"));
	 		host = prop.getProperty("host", "localhost");
	 	
	 		input.close();
		} catch (Exception ex) {
			logger.error("Error reading config", ex);
		} 
	}

	public void connect(String db) throws UnknownHostException {
		domain = db;
		String mongoCompatibleDB = db.replaceAll("\\.", "_");
		System.out.println("connecting to mongodb on " + host + ":" + port + " using db " + mongoCompatibleDB);
		connection = new MongoClient( host, port );
		dbConnection = connection.getDB( mongoCompatibleDB );
		
		if(!dbConnection.collectionExists("crawls")) {
			dbConnection.createCollection("crawls", null);			
		}		
		if(!dbConnection.collectionExists("crawlwords")) {
			dbConnection.createCollection("crawlwords", null);			
		}		
		if(!dbConnection.collectionExists("crawlresults")) {
			dbConnection.createCollection("crawlresults", null);
		}
		if(!dbConnection.collectionExists("links")) {
			dbConnection.createCollection("links", null);
		}
		if(!dbConnection.collectionExists("errors")) {
			dbConnection.createCollection("errors", null);
		}
		
		wordsCollection = dbConnection.getCollection("crawlwords");
		crawlsCollection = dbConnection.getCollection("crawls");
		resultCollection = dbConnection.getCollection("crawlresults");
		linkCollection = dbConnection.getCollection("links");
		errorCollection = dbConnection.getCollection("errors");
		
	}
	
	public void crawlDone() {
		
		BasicDBObject searchQuery = new BasicDBObject().append(CRAWLID, crawlId);

		String endString = format.format(new Date());			
		BasicDBObject newDocument1 = new BasicDBObject();
		newDocument1.append("$set", new BasicDBObject().append(ENDED, endString));	 
		crawlsCollection.update(searchQuery, newDocument1);

		BasicDBObject newDocument2 = new BasicDBObject();
		newDocument2.append("$set", new BasicDBObject().append(DONE, "true"));	 
		crawlsCollection.update(searchQuery, newDocument2);
	}

	public List<Crawl> getUnfinishedCrawls() {
		List<Crawl> list = new LinkedList<Crawl>();
		DBCursor cursor = crawlsCollection.find();
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
			
				String id = (String) row.get(CRAWLID);
				String isFinished = (String) row.get(DONE);
				String started = (String) row.get(STARTED);
				String ended = (String) row.get(ENDED);

				if("false".equals(isFinished)) {
					Crawl crawl = new Crawl(id, isFinished.equals("true"), started, ended);
					list.add(crawl);
				}
			}		
		} finally {
			cursor.close();
		}
		return list;
		
	}
	
	public List<Crawl> getFinishedCrawls() {
		List<Crawl> list = new LinkedList<Crawl>();
		DBCursor cursor = crawlsCollection.find();
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
			
				String id = (String) row.get(CRAWLID);
				String isFinished = (String) row.get(DONE);
				String started = (String) row.get(STARTED);
				String ended = (String) row.get(ENDED);

				if("true".equals(isFinished)) {
					Crawl crawl = new Crawl(id, isFinished.equals("true"), started, ended);
					list.add(crawl);
				}
			}		
		} finally {
			cursor.close();
		}
		return list;
		
	}

	public List<Crawl> getAllCrawls() {

		List<Crawl> list = new LinkedList<Crawl>();
		DBCursor cursor = crawlsCollection.find();
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
			
				String id = (String) row.get(CRAWLID);
				String isFinished = (String) row.get(DONE);
				String started = (String) row.get(STARTED);
				String ended = (String) row.get(ENDED);
				
				Crawl crawl = new Crawl(id, isFinished.equals("true"), started, ended);
				list.add(crawl);
			}		
		} finally {
			cursor.close();
		}
		return list;
	}
	
	public void continueCrawl(String crawlId) {
		this.crawlId = crawlId;
	}
	
	public void newCrawl(Map<String, List<String>> words) {
		String maxId = null;
		DBCursor cursor = crawlsCollection.find();
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();

				String candidateId = (String) row.get(CRAWLID);
				if(maxId == null) {
					maxId = candidateId;
				} else {
					if(Integer.parseInt(candidateId) > Integer.parseInt(maxId)) {
						maxId = candidateId;
					}
				}		
			}
		} finally {
			cursor.close();
		}
			
		String id = "1";
		if(maxId != null) {
			id = "" + (Integer.parseInt(maxId) + 1);
		}

		String startString = format.format(new Date());			
		BasicDBObject doc = new BasicDBObject(CRAWLID, id).append(DONE, "false").append(STARTED, startString).append(ENDED, "");
		crawlsCollection.insert(doc);

		BasicDBObject wordDoc = new BasicDBObject(CRAWLID, id);
		wordDoc.append("nn", words.get("nn"));
		wordDoc.append("bm", words.get("bm"));
		wordDoc.append("en", words.get("en"));
		
		wordsCollection.insert(wordDoc);
		
		crawlId = id;
	}
	
	public boolean alreadyParsed(String url) {
		url = Util.safe(url);
		System.out.println("aleadyParsed: " + url);
		BasicDBObject query = new BasicDBObject(CRAWLID, crawlId).append(URL, url);

		DBCursor cursor = resultCollection.find(query);

		try {
		   while(cursor.hasNext()) {
			   return true;
		   }
		} finally {
		   cursor.close();
		}
		
		cursor = linkCollection.find(query);

		try {
		   while(cursor.hasNext()) {
			   return true;
		   }
		} finally {
		   cursor.close();
		}
		
		cursor = errorCollection.find(query);

		try {
		   while(cursor.hasNext()) {
			   return true;
		   }
		} finally {
		   cursor.close();
		}

		return false;
	} 
	
	public synchronized String getNextLink() {
		System.out.println("getNextLink");

		BasicDBObject searchQuery = new BasicDBObject().append(CRAWLID, crawlId);
		
		DBObject myDoc = linkCollection.findOne(searchQuery);
		if(myDoc != null) {
			String url = (String) myDoc.get(URL);
			linkCollection.remove(myDoc);
			return Util.unsafe(url);
		} 
		return null;
	}
	
	private SinglePage getCrawlWords() {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		
		Map<String, List<String>> words = new HashMap<String, List<String>>();
		
		DBCursor cursor = wordsCollection.find(searchQuery);
		if(cursor.hasNext()) {
			DBObject row = cursor.next();
			List<String> nnWords = (List<String>) row.get("nn");
			List<String> bmWords = (List<String>) row.get("bm");
			List<String> enWords = (List<String>) row.get("en");	
			
			words.put("nn", nnWords);
			words.put("bm", bmWords);
			words.put("en", enWords);
		}
		
		return new SinglePage("", words, 0, 0, 0, 0);
	}
	
	public SinglePage getURLContent(String url) {
		String safeUrl = Util.safe(url);
		
		SinglePage words = getCrawlWords();
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		searchQuery.put(URL, java.util.regex.Pattern.compile(safeUrl));

		DBCursor cursor = resultCollection.find(searchQuery);
		
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
				String content = (String) row.get("content");
				Number wc = (Number) row.get("wordcount");
				Number wcNN = (Number) row.get("wordcountNN");
				Number wcBM = (Number) row.get("wordcountBM");
				Number wcEN = (Number) row.get("wordcountEN");
				
				return new SinglePage(content, words.words, wc.longValue(), wcNN.longValue(), wcBM.longValue(), wcEN.longValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return new SinglePage("", words.words, 0, 0, 0, 0);
	}
	
	public List<PartialCrawlResult> getDetailResult(int level, String filter) {
		String safeFilter = Util.safe(filter);
		System.out.println("getDetailResult(level:" + level + ", filter:" + filter +")");
		List<PartialCrawlResult> resList = new LinkedList<PartialCrawlResult>();

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		searchQuery.put(URL, java.util.regex.Pattern.compile(safeFilter));
		
		DBObject fields = new BasicDBObject("url", 1);
		
		DBCursor cursor = resultCollection.find(searchQuery, fields);

		UrlTree urlTree = new UrlTree();
		
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
				String url = (String) row.get(URL);
					
				urlTree.addUrl(Util.unsafe(url));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HashMap<String, Integer> urlParts = urlTree.getParts(level, filter);
		for(String url : urlParts.keySet()) {
			if(urlParts.get(url) > 0) {
				resList.add(getPartialResult(url,  urlParts.get(url)));
			} else {
				resList.add(getPartialResultSinglePage(url));				
			}
		}
		
		return resList;
	}

	private PartialCrawlResult getPartialResultSinglePage(String url) {
		
		String safeUrl = Util.safe(url);
		System.out.println("getPartialResultSinglePage(url:" + url + " -- " + safeUrl + ")");

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		searchQuery.put(URL, java.util.regex.Pattern.compile(safeUrl));

		DBCursor cursor = resultCollection.find(searchQuery);

		try {
			if(cursor.hasNext()) {
				DBObject row = cursor.next();
	
				Number wc = (Number) row.get("wordcount");
				Number wcNN = (Number) row.get("wordcountNN");
				Number wcBM = (Number) row.get("wordcountBM");
				Number wcEN = (Number) row.get("wordcountEN");
		
				return new PartialCrawlResult(url, 0, wc.longValue(), wcNN.longValue(), wcBM.longValue(), wcEN.longValue());						
			}
		} finally {
			cursor.close();
		}

		return new PartialCrawlResult(url, 0, 0, 0, 0, 0);		
	}
	
	private PartialCrawlResult getPartialResult(String url, int totalPages) {
		String safeUrl = Util.safe(url);
		System.out.println("getPartialResult(url:" + url + ", totalPages:" + totalPages +")");
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		searchQuery.put(URL, java.util.regex.Pattern.compile(safeUrl));
		
		DBObject fields = new BasicDBObject("wordcount", 1);
		fields.put("wordcountNN", 1);
		fields.put("wordcountBM", 1);
		fields.put("wordcountEN", 1);		
		DBObject project = new BasicDBObject("$project", fields );

		DBObject groupFields = new BasicDBObject( "_id", null);
		groupFields.put("sumTotal", new BasicDBObject( "$sum", "$wordcount"));
		groupFields.put("sumNN", new BasicDBObject( "$sum", "$wordcountNN"));
		groupFields.put("sumBM", new BasicDBObject( "$sum", "$wordcountBM"));
		groupFields.put("sumEN", new BasicDBObject( "$sum", "$wordcountEN"));
		DBObject group = new BasicDBObject("$group", groupFields);

		DBObject match = new BasicDBObject("$match", searchQuery);
		
		List<DBObject> pipeline = Arrays.asList(match, project, group);

			AggregationOutput output = resultCollection.aggregate(pipeline);

		for (DBObject result : output.results()) {

			Number total = (Number) result.get("sumTotal");
			Number nn = (Number) result.get("sumNN");
			Number bm = (Number) result.get("sumBM");
			Number en = (Number) result.get("sumEN");
			
			return new PartialCrawlResult(url, totalPages, total.longValue(), nn.longValue(), bm.longValue(), en.longValue());			
		}	
		System.out.println("partial not found: " + safeUrl);
		return new PartialCrawlResult(url, totalPages, 0, 0, 0, 0);
	}
	
	private CrawlResult getResultTime() {
		BasicDBObject searchQuery = new BasicDBObject().append(CRAWLID, crawlId);
		DBObject doc = crawlsCollection.findOne(searchQuery);
		String started = (String) doc.get(STARTED);
		if(started == null) {
			started = "";
		}
		String ended = (String) doc.get(ENDED);
		if(ended == null) {
			ended = "";
		}

		return new CrawlResult(crawlId, "", started, ended, -1, -1, -1, -1, -1);
	}
	
	public CrawlResult getResult(String filter) {
		String safeFilter = Util.safe(filter);
		CrawlResult timeRes = getResultTime();
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		if(filter != null && !"".equals(filter)) {
			searchQuery.put(URL, java.util.regex.Pattern.compile(safeFilter));
		}
		
		String url = domain;
		if(filter != null && !"".equals(filter)) {
			url = filter;
		}
		
		int totalPages = resultCollection.find(searchQuery).count();

		DBObject fields = new BasicDBObject("wordcount", 1);
		fields.put("wordcountNN", 1);
		fields.put("wordcountBM", 1);
		fields.put("wordcountEN", 1);		
		DBObject project = new BasicDBObject("$project", fields );

		DBObject groupFields = new BasicDBObject( "_id", null);
		groupFields.put("sumTotal", new BasicDBObject( "$sum", "$wordcount"));
		groupFields.put("sumNN", new BasicDBObject( "$sum", "$wordcountNN"));
		groupFields.put("sumBM", new BasicDBObject( "$sum", "$wordcountBM"));
		groupFields.put("sumEN", new BasicDBObject( "$sum", "$wordcountEN"));
		DBObject group = new BasicDBObject("$group", groupFields);

		DBObject match = new BasicDBObject("$match", searchQuery);
		
		List<DBObject> pipeline = Arrays.asList(match, project, group);
		AggregationOutput output = resultCollection.aggregate(pipeline);
		
		for (DBObject result : output.results()) {

			Number total = (Number) result.get("sumTotal");
			Number nn = (Number) result.get("sumNN");
			Number bm = (Number) result.get("sumBM");
			Number en = (Number) result.get("sumEN");
			
			return new CrawlResult(crawlId, Util.unsafe(url), timeRes.startTime, timeRes.endTime, totalPages, total.longValue(), nn.longValue(), bm.longValue(), en.longValue());
		}
		
		return new CrawlResult(crawlId, Util.unsafe(url), timeRes.startTime, timeRes.endTime, totalPages, 0, 0, 0, 0);

	}
		
	public void insertUnparsedPage(String url) {
		url = Util.safe(url);
		System.out.println("insertUnparsedPage: " + url);

		if(!alreadyParsed(url)) {
			BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, url);
			linkCollection.insert(doc);
		}
	}
	
	public void insertPageFailed(String url, String message) {
		url = Util.safe(url);
		BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, url)
				.append("error", message);

		errorCollection.insert(doc);
	}
	
	public void insertPageResult(String url, String content, int wordCount, int wordCountNN, int wordCountBM, int wordCountEN) {
		url = Util.safe(url);
		BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, url)
        .append("wordcount", wordCount)
        .append("wordcountNN", wordCountNN)
        .append("wordcountBM", wordCountBM)
        .append("wordcountEN", wordCountEN)
        .append("content", content);
        
		resultCollection.insert(doc);
	}
	
}
