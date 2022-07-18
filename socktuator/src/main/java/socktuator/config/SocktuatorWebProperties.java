package socktuator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("socktuator.web")
public class SocktuatorWebProperties {

	public static class WebServerProps {
		int port = 9999;
		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}

	private final Map<String, WebServerProps> servers = new HashMap<>();

	public Map<String, WebServerProps> getServers() {
		return servers;
	}

}
