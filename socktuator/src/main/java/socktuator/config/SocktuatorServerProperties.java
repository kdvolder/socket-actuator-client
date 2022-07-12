package socktuator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("socktuator.socket")
public class SocktuatorServerProperties {
	
	private final Map<String,Server> servers = new HashMap<>();

	public static class Server {
		private boolean enabled = false;
		private int timeout = 10000;
		private String host = "localhost";
		private int port = 7007;
	
		public boolean isEnabled() {
			return enabled;
		}
	
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	
		public int getPort() {
			return port;
		}
	
		public void setPort(int port) {
			this.port = port;
		}
	
		public int getTimeout() {
			return timeout;
		}
	
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}
	
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
	}

	public Map<String,Server> getServers() {
		return servers;
	}
}
