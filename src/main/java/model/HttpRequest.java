package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {

	private String method;
	private String path;
	private Map<String, String> header;
	private Map<String, String> parameter;
	
	public HttpRequest(InputStream in) throws IOException {
		
		header = new HashMap<String, String>();
		parameter = new HashMap<String, String>();

		BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		//요청 - GET /user/create?userId=javajigi&password=password&name=JaeSung HTTP/1.1
		String line = br.readLine(); 
		
		if(line==null){
			return;
		}
		String[] requestTokens = line.split(" ");
		
		method = requestTokens[0];
		
		if(requestTokens[1].contains("?")){
			path = requestTokens[1].split("\\?")[0];
			parameter = HttpRequestUtils.parseQueryString(requestTokens[1].split("\\?")[1]);
		}else{
			path = requestTokens[1];
		}
		
		//헤더
    	while(!"".equals(line = br.readLine())&&!(line == null)){
    		String[] headerToken = line.split(": ");
    		header.put(headerToken[0], headerToken[1]);
    	}
    	
    	//본문
    	if(method.equals("POST")){
    		String requestBody = IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
    		parameter = HttpRequestUtils.parseQueryString(requestBody);
    	}
		
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getHeader(String key) {
		return header.get(key);
	}

	public String getParameter(String key) {
		return parameter.get(key);
	}
	
	
	
}
