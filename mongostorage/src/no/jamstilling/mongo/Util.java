package no.jamstilling.mongo;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Util {

	public static String unsafe(String arg) {
		try {
			return URLDecoder.decode(arg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return arg;
		}
	}
	
	public static String safe(String arg) {
		try {
			return URLEncoder.encode(arg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return arg;
		}
	}
}
