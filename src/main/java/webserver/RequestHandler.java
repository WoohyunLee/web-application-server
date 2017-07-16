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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;

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
        	//요청라인
        	String line = br.readLine();
        	log.debug("firstHeader :{}", line);
        	if(line == null) {
        		return;
        	}
        	
        	//데이터 없는 redirect
        	String requestUrl = HttpRequestUtils.getRequestLine(line);
        	//회원가입
        	if(requestUrl.startsWith("/user/create")){
        		String[] requests = requestUrl.split("\\?");
        		Map<String, String> params = HttpRequestUtils.parseQueryString(requests[1]);
        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        		requestUrl = "/index.html";
        		log.debug("User : {})", user);
        	}
        	/*
        	//요청헤더, 공백, 본문
        	while(!"".equals(line)){
        		line = br.readLine();
        		log.debug("header :{}", line);
        	}
        	*/
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp"+requestUrl).toPath());
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
