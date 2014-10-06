package no.jamstilling.crawler.download;

import java.io.IOException;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class DownloadPDF {

	public static String getBody(String link) throws IOException {

		URL url = new URL(link);
		
		PDFTextStripper stripper = new PDFTextStripper();
		PDDocument document = PDDocument.load(url);
		String text = stripper.getText(document);
		
		document.close();
		return text;
	}
}
