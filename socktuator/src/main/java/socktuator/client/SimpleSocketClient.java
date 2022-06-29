package socktuator.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import socktuator.server.SimpleSocketServer;

public class SimpleSocketClient {
	
	private InetSocketAddress target;
	private ObjectMapper mapper = new ObjectMapper();
	private int so_timeout;

	public SimpleSocketClient(InetSocketAddress target, int so_timeout) {
		this.target = target;
		this.so_timeout = so_timeout;
	}
	
	public Object health() throws IOException {
		return call("health.health", Map.of());
	}
	
	public Object healthForPath(String... path) throws IOException {
		return call("health.healthForPath", Map.of("path", path));
	}
	
	public Object call(String operationId, Map<String, Object> params) throws IOException {
		try (Socket socket = newSocket()) {
			OutputStream out = StreamUtils.nonClosing(socket.getOutputStream());
			InputStream input = StreamUtils.nonClosing(socket.getInputStream());
			mapper.writeValue(out, operationId);
			if (params!=null && !params.isEmpty()) {
				mapper.writeValue(out, params);
			}
			SimpleSocketServer.Response resp = mapper.readValue(input, SimpleSocketServer.Response.class);
			if (resp.getError()!=null) {
				throw new IOException(resp.getError());
			}
			return resp.getResult();
		}
	}

	protected Socket newSocket() throws IOException {
		Socket so = new Socket(target.getAddress(), target.getPort());
		so.setSoTimeout(so_timeout);
		return so;
	}
	
}
