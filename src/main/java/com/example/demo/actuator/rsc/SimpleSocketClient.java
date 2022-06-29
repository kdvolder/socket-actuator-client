package com.example.demo.actuator.rsc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleSocketClient {
	
	private InetSocketAddress target;
	private ObjectMapper mapper = new ObjectMapper();

	public SimpleSocketClient(InetSocketAddress target) {
		this.target = target;
	}
	
	public Object health() throws IOException {
		try (Socket socket = new Socket(target.getAddress(), target.getPort())) {
			OutputStream out = StreamUtils.nonClosing(socket.getOutputStream());
			InputStream input = StreamUtils.nonClosing(socket.getInputStream());
			mapper.writeValue(out, "health.health");
			SimpleSocketServer.Response resp = mapper.readValue(input, SimpleSocketServer.Response.class);
			if (resp.error!=null) {
				throw new IOException(resp.error);
			}
			return resp.result;
		}
	}

}
