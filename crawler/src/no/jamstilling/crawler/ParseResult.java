package no.jamstilling.crawler;

import no.jamstilling.crawler.language.LanguageDefinitions;

public class ParseResult {
	
	public final int totaltAntallOrd;
	
	public final int ordTreffNynorsk;
	public final int ordTreffBokmaal;
	public final int ordTreffEngelsk;

	public final String content;
	public String url = "";
	
	public ParseResult(ParseResult res) {
		this.url = res.url;
		this.totaltAntallOrd = res.totaltAntallOrd;
		this.ordTreffNynorsk = res.ordTreffNynorsk;
		this.ordTreffBokmaal = res.ordTreffBokmaal;
		this.ordTreffEngelsk = res.ordTreffEngelsk;
		this.content = res.content;
	}
	
	public ParseResult(String url, String content, int totaltAntallOrd, int ordTreffNynorsk, int ordTreffBokmaal, int ordTreffEngelsk) {
		this.url = url;
		this.totaltAntallOrd = totaltAntallOrd;
		this.ordTreffNynorsk = ordTreffNynorsk;
		this.ordTreffBokmaal = ordTreffBokmaal;
		this.ordTreffEngelsk = ordTreffEngelsk;
		this.content = content;
	}	
	
	/*
	public String getFileString() {
		return totaltAntallOrd + " " + ordTreffNynorsk + " " + ordTreffBokmaal + " " + ordTreffEngelsk + " " + url + "\n" + lokalFil + "\n";
	}
	*/
	
	
	public double getNynorskProsent() {
		if(ordTreffBokmaal == 0 && ordTreffEngelsk == 0 && ordTreffNynorsk > 0) {
			return 1;
		}
		if(ordTreffNynorsk == 0) {
			return 0;
		}
		return (double)ordTreffNynorsk / ((double)(ordTreffNynorsk + ordTreffBokmaal + ordTreffEngelsk));

	}

	public double getBokmaalProsent() {
		if(ordTreffNynorsk == 0 && ordTreffEngelsk == 0 && ordTreffBokmaal > 0) {
			return 1;
		}
		if(ordTreffBokmaal == 0) {
			return 0;
		}
		return (double)ordTreffBokmaal / ((double)(ordTreffNynorsk + ordTreffBokmaal + ordTreffEngelsk));
	}
	
	public double getEngelskProsent() {
		if(ordTreffNynorsk == 0 && ordTreffEngelsk > 0 && ordTreffBokmaal == 0) {
			return 1;
		}
		if(ordTreffEngelsk == 0) {
			return 0;
		}
		return (double)ordTreffEngelsk / ((double)(ordTreffNynorsk + ordTreffBokmaal + ordTreffEngelsk));
	}
	
	public int getHits(int langType) {
		if(langType == LanguageDefinitions.BOKMAAL) {
			return ordTreffBokmaal;
		} else if(langType == LanguageDefinitions.NYNORSK) {
			return ordTreffNynorsk;
		} else if(langType == LanguageDefinitions.ENGLISH) {
			return ordTreffEngelsk;
		}
		return 0;
	}
	
	public int getWords(int langType) {
		if(langType == LanguageDefinitions.BOKMAAL) {
			return (int) (this.totaltAntallOrd * getBokmaalProsent());
		} else if(langType == LanguageDefinitions.NYNORSK) {
			return (int) (this.totaltAntallOrd * getNynorskProsent());
		} else if(langType == LanguageDefinitions.ENGLISH) {
			return (int) (this.totaltAntallOrd * getEngelskProsent());
		} 
		return 0;
	}
}
