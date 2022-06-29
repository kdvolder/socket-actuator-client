package socktuator.server;

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
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameter;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameters;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import socktuator.config.SocktuatorServerProperties;
import socktuator.discovery.ExposableSocktuatorEndpoint;
import socktuator.discovery.SocktuatorEndpointsSupplier;
import socktuator.discovery.SocktuatorOperation;

public class SimpleSocketServer implements InitializingBean, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(SimpleSocketServer.class);

	private static final int SOCKET_TIMEOUT = 5000;
	
	SocktuatorServerProperties props;
	private ObjectMapper mapper = new ObjectMapper();
	private ServerSocket serverSocket;

	Map<String, SocktuatorOperation> operationsIdx = new HashMap<>();

	public SimpleSocketServer(
			SocktuatorEndpointsSupplier endpointSupplier,
			SocktuatorServerProperties props
	) {
		this.props = props;
		Collection<ExposableSocktuatorEndpoint> endpoints = endpointSupplier.getEndpoints();
		for (ExposableSocktuatorEndpoint ep : endpoints) {
			Collection<SocktuatorOperation> ops = ep.getOperations();
			for (SocktuatorOperation op : ops) {
				String name = op.getName();
				Assert.isTrue(!operationsIdx.containsKey(name), "Duplicate operation with name: "+name);
				operationsIdx.put(name, op);
				log.info("actuator socket operation registered: {}", name);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (props.isEnabled()) {
			serverSocket = new ServerSocket(props.getPort());
			{
				Thread t = new Thread(() -> handleIncomingRequests());
				t.setDaemon(true);
				t.start();
			}
		} else {
			log.warn("Possible (auto)config bug: this bean should not exist because ");
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
		private String error; // an error for a failed invocation
		private Object result; // the return value of a successful invocation
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
				clientSocket.setSoTimeout(SOCKET_TIMEOUT);
				InputStream input = StreamUtils.nonClosing(clientSocket.getInputStream());
				OutputStream out = StreamUtils.nonClosing(clientSocket.getOutputStream());
				try {
					String operationId = mapper.readValue(input, String.class);
					log.info("Request received: "+operationId);
					SocktuatorOperation op = operationsIdx.get(operationId);
					if (op!=null) {
						OperationParameters params = op.getParameters();
						HashMap<String,Object> paramValues = new HashMap<>();
						if (op.getParameters().hasParameters()) {
							JsonNode jsonParamValues = mapper.readTree(input);
							for (OperationParameter param : params) {
								String name = param.getName();
								JsonNode jsonParamVal = jsonParamValues.get(name);
								if (jsonParamVal!=null) {
									Object paramVal = mapper.convertValue(jsonParamVal, param.getType());
									paramValues.put(name, paramVal);
								}
							}
						}
						Object result = op.invoke(new InvocationContext(SecurityContext.NONE, paramValues));
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
