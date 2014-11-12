package no.jamstilling.crawler.download;

import java.io.IOException;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class DownloadPDF {

	public static String getBody(String link) throws IOException {

		System.out.println(link);
		URL url = new URL(link);
		
		PDFTextStripper stripper = new PDFTextStripper();

		LoadWithTimeout lwt = new LoadWithTimeout(url);
		lwt.start();
	
		PDDocument document = null;
		for(int i = 0; i < 60; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			document = lwt.document;
			if(document != null) {
				break;
			}
			if(lwt.exception != null) {
				break;
			}			
		}
	
		if(document != null) {
			String text = stripper.getText(document);
			document.close();
			return text;
		}
		if(lwt.exception != null) {
			throw lwt.exception;
		}
		throw new IOException("Timeout loading " + url);
	}
	
	static class LoadWithTimeout extends Thread {
		private final URL url;
		public IOException exception;
		public PDDocument document = null;
		public LoadWithTimeout(URL url) {
			this.url = url;
		}
		
		public void run() {
			try {
				document = PDDocument.load(url);
			} catch (IOException e) {
				this.exception = e;
			}
		}
	}
	
}
