package no.jamstilling.crawler.language;

public class LanguageDefinitions {

	public static final int NYNORSK = 1;
	public static final int BOKMAAL = 2;
	public static final int UNKNWON = 3;
	public static final int ENGLISH = 4;

	
	public static String asString(int lang) {
		if(lang == LanguageDefinitions.NYNORSK) {
			return "nn"; 
		} else if(lang == LanguageDefinitions.BOKMAAL) {
			return "nb";
		} else if(lang == LanguageDefinitions.ENGLISH) {
			return "eng";
		} else {
			return "uklassifisert";
		}
	}
	
	public static int asInt(String lang) {
		if(lang.equals("nn")) {
			return LanguageDefinitions.NYNORSK;
		} else if(lang.equals("nb")) {
			return LanguageDefinitions.BOKMAAL; 
		} else if(lang.equals("eng")) {
			return LanguageDefinitions.ENGLISH;
		} else {
			return LanguageDefinitions.UNKNWON;
		}
	}
	
}
