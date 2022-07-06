package socktuator.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import socktuator.config.SocktuatorServerProperties;
import socktuator.config.TaskSchedConf;
import socktuator.discovery.ExposableSocktuatorEndpoint;
import socktuator.discovery.SocktuatorEndpointsSupplier;
import socktuator.discovery.SocktuatorOperation;
import socktuator.discovery.SocktuatorOperationParameter;
import socktuator.discovery.SocktuatorOperationRegistry;
import socktuator.dto.OperationMetadata;
import socktuator.dto.OperationMetadata.Param;
import socktuator.dto.Response;
import socktuator.dto.SharedObjectMapper;

public class SimpleSocketServer implements InitializingBean, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(SimpleSocketServer.class);
	private final ThreadPoolTaskScheduler scheduler = TaskSchedConf.get();

	private final SocktuatorServerProperties props;
	private final ObjectMapper mapper = SharedObjectMapper.get();
	private ServerSocket serverSocket;
	private final SocktuatorOperationRegistry operationsIdx;

	public SimpleSocketServer(
			SocktuatorServerProperties props,
			SocktuatorOperationRegistry operationsIdx
	) {
		this.props = props;
		this.operationsIdx = operationsIdx;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (props.isEnabled()) {
			serverSocket = new ServerSocket(props.getPort());
			scheduler.execute(this::dispatchRequests);
		} else {
			log.warn("Possible (auto)config bug: this bean should not exist server is disabled ");
		}
	}
	
	public static class Request {
		private String op;
		private Map<String, Object> params;
		public Request() {}
		public Request(String op, Map<String, Object>  params) {
			this.op = op;
			this.params = params;
		}
		public Object getParams() {
			return params;
		}
		public void setParams(Map<String, Object> params) {
			this.params = params;
		}
		public String getOp() {
			return op;
		}
		public void setOp(String op) {
			this.op = op;
		}
	}

	private void dispatchRequests() {
		while (serverSocket!=null) {
			try {
				final Socket _clientSocket = serverSocket.accept();
				log.info("Server accepted client connection");
				scheduler.execute(() -> {
					try (Socket clientSocket = _clientSocket) {
						if (props.getTimeout()>0) {
							clientSocket.setSoTimeout(props.getTimeout());
						}
						InputStream input = StreamUtils.nonClosing(clientSocket.getInputStream());
						OutputStream out = StreamUtils.nonClosing(clientSocket.getOutputStream());
						Request req = mapper.readValue(input, Request.class);
						log.info("Server Request received: "+req.op);
						SocktuatorOperation op = operationsIdx.get(req.op);
						if (op==null) {
							send(out, Response.error(new UnsupportedOperationException(req.op)));
						} else {
							SocktuatorOperationParameter[] params = op.getParameters();
							HashMap<String,Object> paramValues = new HashMap<>();
							Map<String, Object> jsonParamValues = req.params;
							for (SocktuatorOperationParameter param : params) {
								String name = param.getName();
								Object jsonParamVal = jsonParamValues.get(name);
								if (jsonParamVal!=null) {
									Object paramVal = mapper.convertValue(jsonParamVal, param.getType());
									paramValues.put(name, paramVal);
								}
				 			}
							try {
								Object result = op.invoke(new InvocationContext(SecurityContext.NONE, paramValues));
								send(out, Response.ok(result));
							} catch (Exception e) {
								send(out, Response.error(e));
							}
						}
					} catch (Exception e) {
						log.error("", e);
					}
				});
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	private void send(OutputStream out, Response r) throws StreamWriteException, DatabindException, IOException {
		// This 'buffering' here is not efficient but it ensures we write all or nothing.
		// This avoids sending a 'partial' unparsable json object if the writer might
		// crash in the middle.
		byte[] encoded = mapper.writeValueAsBytes(r);
		out.write(encoded);
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
