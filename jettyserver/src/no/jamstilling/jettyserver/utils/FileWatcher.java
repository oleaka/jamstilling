package no.jamstilling.jettyserver.utils;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import no.jamstilling.jettyserver.handlers.*;
import no.jamstilling.mongo.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileWatcher {

	private final static WatchService watcher = getWatcher();
	private final static Set<Path> areWatched = new HashSet<Path>();
	
	private final static Map<String, List<FileHandler>> fileHandlers = new HashMap<String, List<FileHandler>>();
	
	public static synchronized void registerFile(Path path, FileHandler handler) throws IOException {
		Path catalog = path.getParent();
		if(!areWatched.contains(catalog)) {
			areWatched.add(catalog);
			catalog.register(watcher, ENTRY_MODIFY);	
		}
		
		synchronized (fileHandlers) {
			
			if(!fileHandlers.containsKey(path)) {
				LinkedList<FileHandler> list = new LinkedList<FileHandler>();
				fileHandlers.put(path.getFileName().toString(), list);
			}
			
			fileHandlers.get(path.getFileName().toString()).add(handler);
		}
	}

	private static void processFileChanges() {
		for (;;) {			 
            try {
                WatchKey key = watcher.take();
                Thread.sleep(50);
				
				List<WatchEvent<?>> events = key.pollEvents();
				key.reset();

				for(WatchEvent event : events) {
					WatchEvent<Path> ev = (WatchEvent<Path>)event;
					Path filename = ev.context();
					System.out.println(filename);
					synchronized (fileHandlers) {
						if(fileHandlers.containsKey(filename.getFileName().toString())) {
							for(FileHandler handler : fileHandlers.get(filename.getFileName().toString())) {
								handler.fileChanged(filename);
							}
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
			Util.log("Error initializing watcher service", e);
		}
		return service;
	}
	
}
