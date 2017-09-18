package ab.java.robome.web.common;

public enum HttpHeader {

	CONTENT_TYPE("Content-Type"),
	AUTHORIZATION("Authorization");
	
	private String text;
	
	HttpHeader(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	
}
