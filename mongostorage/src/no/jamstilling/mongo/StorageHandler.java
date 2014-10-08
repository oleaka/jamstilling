package no.jamstilling.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	static final String DONE = "done";
	
	static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	static final Logger logger = LogManager.getLogger(StorageHandler.class.getName());
	
	private int port = 27017;
	private String host = "localhost";
	
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
	
	 		port = Integer.parseInt(prop.getProperty("port"));
	 		host = prop.getProperty("host");
	 		
	 		input.close();
		} catch (Exception ex) {
			logger.error("Error reading config", ex);
		} 
	}

	public void connect(String db) throws UnknownHostException {
	
		String mongoCompatibleDB = db.replaceAll("\\.", "_");
		
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
	
		setupCrawlId();
	}
	
	public void crawlDone() {
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$set", new BasicDBObject().append(DONE, "true"));	 
		BasicDBObject searchQuery = new BasicDBObject().append(CRAWLID, crawlId);
	 
		crawlsCollection.update(searchQuery, newDocument);
	}
	
	private void setupCrawlId() {
		
		String maxId = null;
		DBCursor cursor = crawlsCollection.find();
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();

				String candidateId = (String) row.get(CRAWLID);
				String isFinished = (String)row.get(DONE);
				if(isFinished.equals("false")) {
					crawlId = candidateId;
					return;
				}
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
		BasicDBObject doc = new BasicDBObject(CRAWLID, id).append(DONE, "false").append(STARTED, startString);
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
	
	public void getResult() {
		/*
db.crawlresults.aggregate([{$match: {crawlid: "4"}}, {$group: {_id:null, total
:{$sum: "$wordcount"}, nn:{$sum: "$wordcountNN"}, bm:{$sum: "$wordcountBM"}, en:
{$sum: "$wordcountEN"}}}])
{ "_id" : null, "total" : 634823, "nn" : 13960, "bm" : 15764, "en" : 33 }
>
		 */
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
