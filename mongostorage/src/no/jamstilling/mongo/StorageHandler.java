package no.jamstilling.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import no.jamstilling.mongo.result.Crawl;
import no.jamstilling.mongo.result.CrawlResult;
import no.jamstilling.mongo.result.PartialCrawlResult;
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
		
		if(!dbConnection.collectionExists("crawlresults")) {
			dbConnection.createCollection("crawlresults", null);
		}
		if(!dbConnection.collectionExists("links")) {
			dbConnection.createCollection("links", null);
		}
		if(!dbConnection.collectionExists("errors")) {
			dbConnection.createCollection("errors", null);
		}
		
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
	
	public void newCrawl() {
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
		
		crawlId = id;
	}
	
	public boolean alreadyParsed(String url) {
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
			return url;
		} 
		return null;
	}
	
	public List<PartialCrawlResult> getDetailResult(int level) {
		List<PartialCrawlResult> resList = new LinkedList<PartialCrawlResult>();

		BasicDBObject searchQuery = new BasicDBObject(CRAWLID, crawlId);
		DBObject fields = new BasicDBObject("url", 1);
		
		DBCursor cursor = resultCollection.find(searchQuery);

		UrlTree urlTree = new UrlTree();
		
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
				String url = (String) row.get(URL);
					
				urlTree.addUrl(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HashMap<String, Integer> urlParts = urlTree.getParts(level);
		for(String url : urlParts.keySet()) {
			PartialCrawlResult pRes = getPartialResult(url,  urlParts.get(url));
			resList.add(pRes);
		}
		
		return resList;
	}
	
	private PartialCrawlResult getPartialResult(String url, int totalPages) {
		
		Pattern regex = Pattern.compile("/.*" + url + ".*/"); 
//		searchQuery.put(URL, regex);
//		searchQuery.append(CRAWLID, crawlId);
		
		DBObject match = new BasicDBObject("$match", new BasicDBObject(CRAWLID, crawlId).append(URL, regex));

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

		List<DBObject> pipeline = Arrays.asList(match, project, group);
		AggregationOutput output = resultCollection.aggregate(pipeline);

		for (DBObject result : output.results()) {

			Number total = (Number) result.get("sumTotal");
			Number nn = (Number) result.get("sumNN");
			Number bm = (Number) result.get("sumBM");
			Number en = (Number) result.get("sumEN");
			
			return new PartialCrawlResult(url, totalPages, total.longValue(), nn.longValue(), bm.longValue(), en.longValue());
			
		}	
		return new PartialCrawlResult(url, totalPages, -1, -1, -1, -1);
	}
	
	public CrawlResult getResult() {
		
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
		int totalPages = resultCollection.find(searchQuery).count();
		
		DBObject match = new BasicDBObject("$match", new BasicDBObject(CRAWLID, crawlId));
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

		List<DBObject> pipeline = Arrays.asList(match, project, group);
		AggregationOutput output = resultCollection.aggregate(pipeline);
		
		for (DBObject result : output.results()) {

			Number total = (Number) result.get("sumTotal");
			Number nn = (Number) result.get("sumNN");
			Number bm = (Number) result.get("sumBM");
			Number en = (Number) result.get("sumEN");
			
			return new CrawlResult(crawlId, domain, started, ended, totalPages, total.longValue(), nn.longValue(), bm.longValue(), en.longValue());
			
		}	
		return null;		
	}
	
	public void insertUnparsedPage(String url) {
		System.out.println("insertUnparsedPage: " + url);

		if(!alreadyParsed(url)) {
			BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, url);
			linkCollection.insert(doc);
		}
	}
	
	public void insertPageFailed(String url, String message) {
		BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, url)
				.append("error", message);

		errorCollection.insert(doc);
	}
	
	public void insertPageResult(String url, String content, int wordCount, int wordCountNN, int wordCountBM, int wordCountEN) {
		BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, url)
        .append("wordcount", wordCount)
        .append("wordcountNN", wordCountNN)
        .append("wordcountBM", wordCountBM)
        .append("wordcountEN", wordCountEN)
        .append("content", content);
        
		resultCollection.insert(doc);
	}
	
}
