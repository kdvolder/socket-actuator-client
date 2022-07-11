package socktuator.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("socktuator.rsocket")
public class RSocktuatorProperties {
	
	private final Map<String, Server> server = new HashMap<>();
	
	public static class Server {
		
		//TODO: probably make this a subclass of org.springframework.boot.autoconfigure.rsocket.RSocketProperties.Server
		//  Alternatively maybe we simply follow the same structure.
		// Note: this will require also client creation to provide similar / matching options.
		
		private int port = 7123;
		private String host = "localhost";
		private boolean enabled;
		private Duration timeout = Duration.ofSeconds(5);

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Duration getTimeout() {
			return timeout;
		}

		public void setTimeout(Duration timeout) {
			this.timeout = timeout;
		}
		
	}

	public Map<String, Server> getServer() {
		return server;
	}
	
}
