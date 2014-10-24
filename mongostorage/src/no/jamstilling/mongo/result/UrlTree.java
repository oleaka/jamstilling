package no.jamstilling.mongo.result;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class UrlTree extends UrlTreeNode {
	
	public UrlTree() {
		super(null);
	}
	
	public void addUrl(String url) {
		if(url.startsWith("http://")) {
			url = url.substring(7);
		} else if(url.startsWith("https://")) {
			url = url.substring(8);
		}
		String[] split = url.split("/");

		addChild(split);
	}
	
	public HashMap<String, Integer> getParts(int level) {

		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		/*
		if(level == 0) {
			for(UrlTreeNode child : children) {
				if(child.numberOfChildren > 0) {
					result.put(child.myPart, child.numberOfChildren);
				}
			}
			return result;
		}
		*/
		for(UrlTreeNode child : children) {
			child.getParts(level-1, result, "");
		}
		return result;
	}
	
	/*
	public void printSubParts() {
		printSubParts("");
	}
		
	 */
	public void printTree() {
		printTree(0);
	}
	
}
