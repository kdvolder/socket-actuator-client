package socktuator.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import socktuator.config.TaskSchedConf;
import socktuator.dto.OperationMetadata;
import socktuator.dto.Response;
import socktuator.dto.SharedObjectMapper;
import socktuator.server.SimpleSocketServer.Request;

public class SimpleSocketClient {

	private static final Logger log = LoggerFactory.getLogger(SimpleSocketClient.class);
	
	private InetSocketAddress target;
	private ObjectMapper mapper = SharedObjectMapper.get();
	private int so_timeout;
	
	private ThreadPoolTaskScheduler scheduler = TaskSchedConf.get();

	public SimpleSocketClient(InetSocketAddress target, int so_timeout) {
		this.target = target;
		this.so_timeout = so_timeout;
	}
	
	public Object health() throws Exception {
		return call("health.health", Map.of());
	}
	
	public Object healthForPath(String... path) throws Exception {
		return call("health.healthForPath", Map.of("path", path));
	}
	
	public Object call(String operationId, Map<String, Object> params) throws Exception {
		return scheduler.submit(() -> {
			try (Socket socket = newSocket()) {
				OutputStream out = StreamUtils.nonClosing(socket.getOutputStream());
				InputStream input = StreamUtils.nonClosing(socket.getInputStream());
				Request req = new Request(operationId, params);
				log.debug("Client Sending operation {}...", mapper.writeValueAsString(req));
				mapper.writeValue(out, req);
				log.debug("Client Sent operation {}", mapper.writeValueAsString(req));
				log.debug("Client awating response...");
				String inputBuf = IOUtils.toString(input);
				try {
					Response resp = mapper.readValue(inputBuf, Response.class);
					log.debug("Client received response");
					if (resp.getError()!=null) {
						log.debug("Client received ERROR response");
						throw new IOException(resp.getError());
					}
					log.debug("Client received OK response");
					return resp.getResult();
				} catch (Exception e) {
					log.error("Problem processing response", e);
					throw e;
				}
			}
		}).get();
	}

	protected Socket newSocket() throws IOException {
		Socket so = new Socket(target.getAddress(), target.getPort());
		so.setSoTimeout(so_timeout);
		return so;
	}

	public OperationMetadata[] getEndpointMetadata() throws Exception {
		Object untyped_resp = call("actuator.actuator", Map.of());
		return mapper.convertValue(untyped_resp, OperationMetadata[].class);
	}
}
