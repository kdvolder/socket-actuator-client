package com.example.demo.actuator.rsc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ConditionalOnProperty(name = "spring.actuator.socket.enabled")
@Component
public class SimpleSocketServer implements InitializingBean, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(SimpleSocketServer.class);
	
	@Autowired
	ActuatorSocketProperties props;
	
	private ObjectMapper mapper = new ObjectMapper();
	private ServerSocket serverSocket;

	Map<String, RscOperation> operationsIdx = new HashMap<>();

	public SimpleSocketServer(RscEndpointsSupplier endpointSupplier) {
		Collection<ExposableRscEndpoint> endpoints = endpointSupplier.getEndpoints();
		for (ExposableRscEndpoint ep : endpoints) {
			Collection<RscOperation> ops = ep.getOperations();
			for (RscOperation op : ops) {
				String name = op.getName();
				Assert.isTrue(!operationsIdx.containsKey(name), "Duplicate operation with name: "+name);
				operationsIdx.put(name, op);
				log.info("actuator socket operation registered: {}", name);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		serverSocket = new ServerSocket(props.getPort());
		{
			Thread t = new Thread(() -> handleIncomingRequests());
			t.setDaemon(true);
			t.start();
		}
	}

	public static class Response {
		public String getError() {
			return error;
		}
		public void setError(String error) {
			this.error = error;
		}
		public Object getResult() {
			return result;
		}
		public void setResult(Object result) {
			this.result = result;
		}
		String error; // an error or null if result is ok
		Object result; //
		public Response() {}
		static Response ok(Object result) { 
			Response self = new Response();
			self.result = result;
			return self;
		}
		static Response error(Throwable e) {
			Response self = new Response();
			self.error = e.getMessage();
			if (self.error==null) {
				self.error = e.getClass().getName();
			}
			return self;
		}
	}
	
	private Object handleIncomingRequests() {
		while (serverSocket!=null) {
			try (Socket clientSocket = serverSocket.accept()) {
				InputStream input = StreamUtils.nonClosing(clientSocket.getInputStream());
				OutputStream out = StreamUtils.nonClosing(clientSocket.getOutputStream());
				try {
					String operationId = mapper.readValue(input, String.class);
					log.info("Request received: "+operationId);
					RscOperation op = operationsIdx.get(operationId);
					if (op!=null) {
						Assert.isTrue(!op.getParameters().hasParameters(), "Operations with parameters are not yet supported");
						InvocationContext ctx = new InvocationContext(SecurityContext.NONE, Map.of());
						Object result = op.invoke(ctx);
						send(out, Response.ok(result));
					}
				} catch (Exception e) {
					log.error("", e);
					send(out, Response.error(e));
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return null;
	}

	private void send(OutputStream out, Response r) throws StreamWriteException, DatabindException, IOException {
		mapper.writeValue(out, r);
	}

	@Override
	public void destroy() throws Exception {
		try {
			serverSocket.close();
		} finally {
			serverSocket = null;
		}
	}
	
	
}
