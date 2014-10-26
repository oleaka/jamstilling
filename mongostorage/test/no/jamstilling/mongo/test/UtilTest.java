package no.jamstilling.mongo.test;

import no.jamstilling.mongo.Util;

import org.junit.Test;

public class UtilTest {

	@Test
	public void test() {
		
		String safe = Util.safe("www.uio.no/?vrtx=tags&tag=jul");
		String unsafe = Util.unsafe(safe);
		System.out.println(safe);
		System.out.println(unsafe);
	}
}
