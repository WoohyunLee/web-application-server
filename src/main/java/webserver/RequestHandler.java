package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	
        	BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        	boolean isPost = false;
        	//요청
        	String line = br.readLine();
        	if(line == null) return;
        	if(line.toUpperCase().startsWith("POST")) isPost = true;
        	String url = HttpRequestUtils.getRequestLine(line);
        	log.debug("firstHeader :{}", line);
        	
        	//헤더
        	Map<String, String> headers = new HashMap<String, String>();
        	while(!"".equals(line = br.readLine())){
        		String[] headerToken = line.split(": ");
        		headers.put(headerToken[0], headerToken[1]);
        	}
        	
        	//POST인 경우 처리
        	if(isPost){
        		String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
        		if(url.startsWith("/user/create")){
        			Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
        			User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        			url = "/index.html";
        			log.debug("User : {})", user);
        		}
        	}
        	/*
        	//회원가입
        	if(requestUrl.startsWith("/user/create")){
        		String[] requests = requestUrl.split("\\?");
        		Map<String, String> params = HttpRequestUtils.parseQueryString(requests[1]);
        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        		requestUrl = "/index.html";
        		log.debug("User : {})", user);
        	}
        	*/
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
