package no.jamstilling.jettyserver;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import no.jamstilling.jettyserver.handlers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileWatcher {

	static final Logger logger = LogManager.getLogger(FileWatcher.class.getName());

	private final static WatchService watcher = getWatcher();
	private final static Map<WatchKey, Path> watching=new HashMap<WatchKey, Path>();
	
	private final static Map<String, List<FileContentHandler>> fileHandlers = new HashMap<String, List<FileContentHandler>>();
	
	public static void registerFile(Path path, FileContentHandler handler) throws IOException {
		Path catalog = path.getParent();
		WatchKey key = catalog.register(watcher, ENTRY_MODIFY);	
		watching.put(key, path);
		
		synchronized (fileHandlers) {
			String absName = path.toFile().getAbsolutePath();
			
			if(!fileHandlers.containsKey(absName)) {
				LinkedList<FileContentHandler> list = new LinkedList<FileContentHandler>();
				fileHandlers.put(absName, list);
			}
			
			fileHandlers.get(absName).add(handler);
		}
	}

	private static void processFileChanges() {
		for (;;) {			 
            try {
                WatchKey key = watcher.take();
                Thread.sleep(20);
				
				List<WatchEvent<?>> events = key.pollEvents();
				key.reset();
				
				Path changedPath=watching.get(key);
				String absName = changedPath.toFile().getAbsolutePath();
				synchronized (fileHandlers) {
					if(fileHandlers.containsKey(absName)) {
						for(FileContentHandler handler : fileHandlers.get(absName)) {
							handler.fileChanged(changedPath);
						}
					}
				}
            } catch (InterruptedException x) {
            }
		}
	}
	
	
	private static WatchService getWatcher() {
		WatchService service = null;
		try {
			service = FileSystems.getDefault().newWatchService();

			new Thread(new Runnable() {
				public void run() {
					processFileChanges();
				}
			}).start();
		} catch (IOException e) {
			logger.error("Error initializing watcher service", e);
		}
		return service;
	}
	
}
