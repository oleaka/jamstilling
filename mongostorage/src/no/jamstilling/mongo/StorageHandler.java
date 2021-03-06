package no.jamstilling.mongo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import no.jamstilling.mongo.result.Crawl;
import no.jamstilling.mongo.result.CrawlResult;
import no.jamstilling.mongo.result.PartialCrawlResult;
import no.jamstilling.mongo.result.SinglePage;
import no.jamstilling.mongo.result.UrlTree;

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
	static final String CONTENT = "content";
	
	private Map<String, ResultBuffer> resultBuffer = new HashMap<String, ResultBuffer>();
	private Set<String> nextLinks = new HashSet<String>();
	private Set<String> done = new HashSet<String>();

	//private Map<String, Integer> parsedCache = new HashMap<String, Integer>();
	
	static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	private int port = 27017;
	private String host = "localhost";
	
	private String domain = null;
	private MongoClient connection = null;
	private DB dbConnection = null;
	private DBCollection resultCollection = null;
	private DBCollection linkCollection = null;
	private DBCollection errorCollection = null;
	private DBCollection crawlsCollection = null;
	private DBCollection contentCollection = null;
	private DBCollection wordsCollection = null;
	
	private String crawlId = null;
	
	private Set<String> activeUrls = new HashSet<String>();
	
	private Object cachelock = new Object(); 
	private Object dblock = new Object(); 
	
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
		 	Util.log("Error reading config", ex);
		} 
	}

	public void connect() throws UnknownHostException {
		connection = new MongoClient( host, port );
	}
	
	public List<String> getAllDomains() {
		List<String> allDomains = new LinkedList<String>();
		
		List<String> dbs = connection.getDatabaseNames();
		
		for(String db : dbs) {
			try {
				
				dbConnection = connection.getDB( db );
					
				DBCollection coll = dbConnection.getCollection("crawls");
				if(coll != null) {					
					BasicDBObject searchQuery = new BasicDBObject();
					int crawlCount = coll.find(searchQuery).count();
					if(crawlCount > 0) {
						String realName = db.replaceAll("_", "\\.");
						allDomains.add(realName);
					}
				}
				
			} catch (Exception e) {}
		}
		
		return allDomains;
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
		if(!dbConnection.collectionExists("contentcollection")) {
			dbConnection.createCollection("contentcollection", null);
		}
	//	if(!dbConnection.collectionExists("links")) {
	//		dbConnection.createCollection("links", null);
	//	}
		if(!dbConnection.collectionExists("errors")) {
			dbConnection.createCollection("errors", null);
		}
		
		wordsCollection = dbConnection.getCollection("crawlwords");
		crawlsCollection = dbConnection.getCollection("crawls");
		
		BasicDBObject index = new BasicDBObject();
		index.put(URL, 1);
		index.put(CRAWLID, -1);
		
		resultCollection = dbConnection.getCollection("crawlresults");
		resultCollection.ensureIndex(index, new BasicDBObject("unique", true));
		
		contentCollection = dbConnection.getCollection("contentcollection");
		contentCollection.ensureIndex(index, new BasicDBObject("unique", true));
		
		linkCollection = dbConnection.getCollection("links");
		linkCollection.ensureIndex(index, new BasicDBObject("unique", true));
		
		errorCollection = dbConnection.getCollection("errors");
		
	}
	
	public void crawlDone() {
		storeResultBuffer();
		
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
	
	public Map<String, List<String>> getWords() {
		
		BasicDBObject query = new BasicDBObject(CRAWLID, crawlId);
		
		DBObject obj = wordsCollection.findOne(query);
	
		List<String> nn = (List<String>) obj.get("nn");
		List<String> bm = (List<String>) obj.get("bm");
		List<String> en = (List<String>) obj.get("en");
		
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		res.put("nn", nn);
		res.put("bm", bm);
		res.put("en", en);
		
		return res;
	}

	/*
	private void cleanParsedCache() {
		synchronized (cachelock) {
		
			int removeCounter = 1;
			
			while(parsedCache.size() > 1500) {
				List<String> urls = new LinkedList<String>(parsedCache.keySet());
				for(String url : urls) {
					if(parsedCache.get(url) == removeCounter) {
						parsedCache.remove(url);
					}
				}
				removeCounter++;
			}			
		}
	}
	*/
	
	private boolean alreadyParsed(String url) {
		url = Util.safe(url);
		
		synchronized (done) {
			if(done.contains(url)) {
				return true;
			}
		}
		
		synchronized (activeUrls) {
			if(activeUrls.contains(url)) {
				return true;	
			}			
		}

		/*
		if(parsedCache.size() > 2000) {
			cleanParsedCache();
		}
		*/
		
		synchronized (cachelock) {
			/*
			if(parsedCache.containsKey(url)) {
				parsedCache.put(url, parsedCache.get(url) + 1);
				return true;
			} else {
				parsedCache.put(url, 1);
			}
			*/

			/*
			if(resultBuffer.containsKey(url)) {
				System.out.println("in resultBuffer aleadyParsed: " + url);
				return true;
			}
			*/

			if(nextLinks.contains(url)) {
				System.out.println("in nextLink aleadyParsed: " + url);
				return true;
			}
		}

		synchronized (dblock) {				

			BasicDBObject query = new BasicDBObject(CRAWLID, crawlId).append(URL, url);
			/*
			DBObject obj = resultCollection.findOne(query);
			if(obj != null) {
				System.out.println("in resultCollection aleadyParsed: " + url);
				return true;
			}
			*/
			
			DBObject obj = linkCollection.findOne(query);
			if(obj != null) {
				System.out.println("in linkCollection aleadyParsed: " + url);
				return true;
			}
		}

		return false;
	} 
	
	public String getNextLink() {
		boolean empty = false;
		synchronized (cachelock) {
			if(nextLinks.isEmpty()) {
				BasicDBObject searchQuery = new BasicDBObject(CRAWLID, crawlId);
				DBCursor cursor = linkCollection.find(searchQuery);
				List<DBObject> toDelete = new LinkedList<DBObject>();
				int counter = 0;
				while(cursor.hasNext() && counter < 100) {
					DBObject row = cursor.next();
					String url = (String) row.get(URL);
					nextLinks.add(url);
					
					toDelete.add(row);
					counter++;
				}

				
				for(DBObject obj : toDelete) {
					linkCollection.remove(obj);					
				}
				
				if(counter < 100) {
					empty = true;
				}
			}
						
			while(!nextLinks.isEmpty()) {
				String link = nextLinks.iterator().next();
				nextLinks.remove(link);
				if(!done.contains(link)) {
					activeUrls.add(link);
					return Util.unsafe(link);
				}
			}
		}
		if(!empty) {
			return getNextLink();
		}
		return null;
	}
		
	public void insertUnparsedPage(String url) {
		url = Util.safe(url);
		if(!alreadyParsed(url)) {
			//System.out.println("insertUnparsedPage: " + url);
			synchronized (cachelock) {
				nextLinks.add(url);
				
				if(nextLinks.size() > 1000) {
					List<DBObject> documents = new LinkedList<DBObject>();

					Iterator<String> it = nextLinks.iterator();
					List<String> toRemove = new LinkedList<String>();
					for(int i = 0; i < 300; i++) {
						String link = it.next();
						toRemove.add(link);
						BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, link);
						documents.add(doc);
					}
					
					nextLinks.removeAll(toRemove);
					
					/*
					for(int i = 0; i < 200; i++) {
						String storeUrl = nextLinks.remove(nextLinks.size() - 1);						
						BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, storeUrl);
						documents.add(doc);
					}
					*/
					
					synchronized (dblock) {
						linkCollection.insert(documents);						
					}
				}
			}
		}
	}
	
	public void insertPageFailed(String url, String message) {
		url = Util.safe(url);
		activeUrls.remove(url);
		BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, url)
				.append("error", message);

		errorCollection.insert(doc);
	}
	
	public void insertPageResult(String url, String content, int wordCount, int wordCountNN, int wordCountBM, int wordCountEN) {
		url = Util.safe(url);
		activeUrls.remove(url);
		synchronized (done) {
			if(done.contains(url)) {
				return;
			}
			done.add(url);			
		}
		
		ResultBuffer buffer = new ResultBuffer(url, content, wordCount, wordCountNN, wordCountBM, wordCountEN);
		synchronized (cachelock) {
			resultBuffer.put(url, buffer);
		}
		if(resultBuffer.size() > 100) {
			storeResultBuffer();
		}
	}

	private void storeResultBuffer() {
		  
		List<DBObject> documents = new LinkedList<DBObject>();
		List<DBObject> contentDocuments = new LinkedList<DBObject>();
				
		synchronized (cachelock) {
		
			for(ResultBuffer res : resultBuffer.values()) {
				BasicDBObject doc = new BasicDBObject(CRAWLID, crawlId).append(URL, res.url)
				        .append("wordcount", res.wordCount)
				        .append("wordcountNN", res.wordCountNN)
				        .append("wordcountBM", res.wordCountBM)
				        .append("wordcountEN", res.wordCountEN);

				documents.add(doc);
				
				BasicDBObject contentDoc = new BasicDBObject(CRAWLID, crawlId).append(URL, res.url).append(CONTENT, res.content);
				contentDocuments.add(contentDoc);				
			}
			resultBuffer.clear();
		}

		if(documents.size() > 0) {
			synchronized (dblock) {
				contentCollection.insert(contentDocuments);				
				resultCollection.insert(documents);				
			}
		}
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
	
	private String getContent(String url) {
		//System.out.println("getContent(" + url + ")");
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		searchQuery.put(URL, url);

		DBCursor cursor = contentCollection.find(searchQuery).limit(1);
		
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
				String content = (String) row.get(CONTENT);
				//System.out.println("found content");
				return content;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return "";
	}
	
	public SinglePage getURLContent(String url) {
		String safeUrl = Util.safe(url);
		//System.out.println("getURLContent(" + url + " => " + safeUrl + ")");
		SinglePage words = getCrawlWords();
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		searchQuery.put(URL, java.util.regex.Pattern.compile(safeUrl));
		
		DBCursor cursor = resultCollection.find(searchQuery).limit(1);
		
		try {
			while(cursor.hasNext()) {
				DBObject row = cursor.next();
				String actualUrl  = (String) row.get(URL);
				String content = getContent(actualUrl);
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
		//System.out.println("getDetailResult(level:" + level + ", filter:" + filter +")");
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
		//System.out.println("getPartialResultSinglePage(url:" + url + " -- " + safeUrl + ")");

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(CRAWLID, crawlId);
		searchQuery.put(URL, java.util.regex.Pattern.compile(safeUrl));
		
		DBCursor cursor = resultCollection.find(searchQuery).limit(1);

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
		//System.out.println("getPartialResult(url:" + url + ", totalPages:" + totalPages +")");
		
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
	
	private class ResultBuffer {
		public final String url;
		public final String content;
		public final int wordCount;
		public final int wordCountNN;
		public final int wordCountBM;
		public final int wordCountEN;
		
		public ResultBuffer(String url, String content, int wordCount, int wordCountNN, int wordCountBM, int wordCountEN) {
			this.url = url;
			this.content = content;
			this.wordCount = wordCount;
			this.wordCountNN = wordCountNN;
			this.wordCountBM = wordCountBM;
			this.wordCountEN = wordCountEN;
		}
	}
}