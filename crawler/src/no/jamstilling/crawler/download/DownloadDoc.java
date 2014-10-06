package no.jamstilling.crawler.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadDoc {

	private final String link;

	public DownloadDoc(String link) {
		this.link = link;
	}
	
	public String download() {
		return null;
		/*
		String filename = StorageManager.STORAGE_BASE_PATH + File.separator + System.currentTimeMillis() + ".doc";
		
		try {
			URL url = new URL(link);

			byte[] ba1 = new byte[1024];
			int baLength;
			FileOutputStream fos = new FileOutputStream(filename);

			// Contacting the URL
			System.out.print("Connecting to " + url.toString() + " ... ");
			URLConnection urlConn = url.openConnection();

			// Checking whether the URL contains a PDF
			if (!urlConn.getContentType().equalsIgnoreCase("application/doc")) {
				System.out.println("FAILED.\n[Sorry. This is not a doc.]");
			} else {
				// Read the PDF from the URL and save to a local file
				InputStream is = url.openStream();
				while ((baLength = is.read(ba1)) != -1) {
					fos.write(ba1, 0, baLength);
				}
				fos.flush();
				fos.close();
				is.close();
			}
			return filename;
		} catch (NullPointerException npe) {
			System.out.println("FAILED.\n[" + npe.getMessage() + "]\n");
		} catch (IOException e) {
			System.out.println("FAILED.\n[" + e.getMessage() + "]\n");
		}
		return null;
		*/
	}
	
}
