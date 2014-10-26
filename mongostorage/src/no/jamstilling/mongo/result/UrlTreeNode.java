package no.jamstilling.mongo.result;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class UrlTreeNode {
	
	protected List<UrlTreeNode> children = new LinkedList<UrlTreeNode>();

	protected final String myPart;
	protected int numberOfChildren = 0;

	public UrlTreeNode(String myPart) {
		this.myPart = myPart;
	}

	protected void addChild(String[] split) {
		if(split.length == 0) {
			return;
		}

		UrlTreeNode child = getChild(split[0]);
		if(split.length > 1) {
			child.addChild(Arrays.copyOfRange(split, 1, split.length));
		}		
	}
	
	protected UrlTreeNode getChild(String part) {
		this.numberOfChildren++;
		for(UrlTreeNode child : children) {
			if(part.equals(child.myPart)) {				
				return child;
			}
		}
		UrlTreeNode child = new UrlTreeNode(part);
		children.add(child);
		return child;
	}

	protected void printTree(int index) {
		for(int i = 0; i < index; i++) {
			System.out.print(" ");
		}
		System.out.println(myPart + ": " + numberOfChildren);
		for(UrlTreeNode child : children) {
			child.printTree(index++);
		}
	}
	
	private void checkAdd(HashMap<String, Integer> urls, String url, String filter, int numberOfChildren) {
		if(url.contains(filter)) {
			urls.put(url, numberOfChildren);
		}
 	}
	
	protected void getParts(int level, HashMap<String, Integer> urls, String soFar, String filter) {
		if(level < 0) {
			return;
		}
		if(level == 0 && numberOfChildren > 0) {
			if("".equals(soFar)) {
				checkAdd(urls, myPart, filter, numberOfChildren);
			} else {
				checkAdd(urls, soFar + "/" + myPart, filter, numberOfChildren);
			}
			return;
		} else if(level == 0 && numberOfChildren == 0) {
			checkAdd(urls, soFar + "/" + myPart, filter, numberOfChildren);			
		}
		for(UrlTreeNode child : children) {
			if("".equals(soFar)) {
				child.getParts(level-1, urls, myPart, filter);
			} else {
				child.getParts(level-1, urls, soFar + "/" + myPart, filter);
			}
		}
	}

	protected void printSubParts(String soFar) {
		if(children.size() > 0) {
			if(myPart != null) {
				if(!"".equals(soFar)) {
					System.out.println(soFar + "/" + myPart + " => " + numberOfChildren);
					for(UrlTreeNode child : children) {
						child.printSubParts(soFar + "/" + myPart);
					}
				} else {
					System.out.println(myPart + " => " + numberOfChildren);
					for(UrlTreeNode child : children) {
						child.printSubParts(myPart);
					}
				}
			} else {
				for(UrlTreeNode child : children) {
					child.printSubParts("");
				}
			}
		}
	}
	
}
