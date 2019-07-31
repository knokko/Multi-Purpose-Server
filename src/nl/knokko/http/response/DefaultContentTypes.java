package nl.knokko.http.response;

import java.util.HashMap;
import java.util.Map;

public class DefaultContentTypes {
	
	private static final Map<String,String> MAP = new HashMap<String,String>();
	
	static {
		MAP.put("html", "text/html");
		MAP.put("htm", "text/html");
		MAP.put("js", "application/javascript");
		MAP.put("wasm", "application/wasm");
		MAP.put("css", "text/css");
		
		MAP.put("pdf", "application/pdf");
		MAP.put("json", "application/json");
		MAP.put("xml", "application/xml");
		MAP.put("txt", "text/plain");
		
		MAP.put("gif", "image/gif");
		MAP.put("png", "image/png");
		MAP.put("jpeg", "image/jpeg");
		MAP.put("jpg", "image/jpeg");
	}
	
	public static Map<String,String> get(){
		return new HashMap<String,String>(MAP);
	}
}