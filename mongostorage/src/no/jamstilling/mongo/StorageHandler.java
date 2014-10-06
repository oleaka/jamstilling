package no.jamstilling.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class StorageHandler {

	static final Logger logger = LogManager.getLogger(StorageHandler.class.getName());
	
	private int port = 27017;
	private String host = "localhost";
	
	private MongoClient connection = null;
	private DB dbConnection = null;
	private DBCollection resultCollection = null;
	
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
		} catch (IOException ex) {
			logger.error("Error reading config", ex);
		} 
	}

	public void connect(String db) throws UnknownHostException {
		connection = new MongoClient( host, port );
		dbConnection = connection.getDB( db );
		resultCollection = dbConnection.getCollection("parseresult");
	}
	
	public boolean alreadyParsed(String url) {
		BasicDBObject query = new BasicDBObject("url", url);

		DBCursor cursor = resultCollection.find(query);

		try {
		   while(cursor.hasNext()) {
			   return true;
		   }
		} finally {
		   cursor.close();
		}
		return false;
	} 
	
	public void insertPageResult(String url, String content, int wordCount, int wordCountNN, int wordCountBM, int wordCountEN) {
		BasicDBObject doc = new BasicDBObject("url", url)
        .append("wordcount", wordCount)
        .append("wordcountNN", wordCountNN)
        .append("wordcountBM", wordCountBM)
        .append("wordcountEN", wordCountEN)
        .append("content", content);
        
		resultCollection.insert(doc);
	}
}
