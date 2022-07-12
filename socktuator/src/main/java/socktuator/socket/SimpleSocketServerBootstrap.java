package socktuator.socket;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import socktuator.config.SocktuatorServerProperties;
import socktuator.config.SocktuatorServerProperties.Server;
import socktuator.discovery.SocktuatorOperationRegistry;

public class SimpleSocketServerBootstrap implements SmartLifecycle, DisposableBean  {
	
	private static final Logger log = LoggerFactory.getLogger(SimpleSocketServerBootstrap.class);
	
	List<SimpleSocketServer> servers = new ArrayList<>(1);
	private SocktuatorServerProperties props;
	private SocktuatorOperationRegistry endpoints;

	public SimpleSocketServerBootstrap(
			SocktuatorServerProperties props, 
			SocktuatorOperationRegistry endpoints
	) {
		this.props = props;
		this.endpoints = endpoints;
	}

	@Override
	public void start() {
		if (isRunning()) {
			throw new IllegalStateException("Already started!");
		}
		Exception firstError = null;
		for (Server serverProps : props.getServers().values()) {
			SimpleSocketServer server = new SimpleSocketServer(serverProps, endpoints);
			try {
				server.afterPropertiesSet();
				servers.add(server);
			} catch (Exception e) {
				log.error("", e);
				if (firstError==null) {
					firstError = e;
				}
			}
		}
		if (firstError!=null) {
			throw new RuntimeException(firstError);
		}
	}

	@Override
	public void stop() {
		List<SimpleSocketServer> toStop = servers;
		servers = new ArrayList<>(toStop.size());
		for (SimpleSocketServer s : toStop) {
			try {
				s.destroy();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	public boolean isRunning() {
		for (SimpleSocketServer s : servers) {
			if (s.isRunning()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void destroy() throws Exception {
		stop();
	}

}
