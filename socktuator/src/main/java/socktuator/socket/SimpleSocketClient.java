package socktuator.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import socktuator.dto.OperationMetadata;
import socktuator.dto.Request;
import socktuator.dto.Response;
import socktuator.dto.SharedObjectMapper;

public class SimpleSocketClient {

	private static final Logger log = LoggerFactory.getLogger(SimpleSocketClient.class);
	
	private InetSocketAddress target;
	private ObjectMapper mapper = SharedObjectMapper.get();
	private int so_timeout;
	
	private Scheduler scheduler = Schedulers.boundedElastic();

	public SimpleSocketClient(InetSocketAddress target, int so_timeout) {
		this.target = target;
		this.so_timeout = so_timeout;
	}
	
	public Mono<Object> health_mono() {
		return call_mono("health.health", Map.of());
	}
	
	public Mono<Object> healthForPath_mono(String... path) {
		return call_mono("health.healthForPath", Map.of("path", path));
	}
	
	public Mono<Object> call_mono(String operationId, Map<String, Object> params) {
		return Mono.fromCallable(() -> {
			try (Socket socket = newSocket()) {
				OutputStream out = StreamUtils.nonClosing(socket.getOutputStream());
				InputStream input = StreamUtils.nonClosing(socket.getInputStream());
				Request req = new Request(operationId, params);
				log.debug("Client Sending operation {}...", mapper.writeValueAsString(req));
				mapper.writeValue(out, req);
				log.debug("Client Sent operation {}", mapper.writeValueAsString(req));
				log.debug("Client awating response...");
				String inputBuf = IOUtils.toString(input, StandardCharsets.UTF_8);
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
		}).subscribeOn(scheduler);
	}

	protected Socket newSocket() throws IOException {
		Socket so = new Socket(target.getAddress(), target.getPort());
		so.setSoTimeout(so_timeout);
		return so;
	}

	public Mono<OperationMetadata[]> getEndpointMetadata() {
		return call_mono("actuator.actuator", Map.of())
		.map(untyped_resp -> mapper.convertValue(untyped_resp, OperationMetadata[].class));
	}
}
