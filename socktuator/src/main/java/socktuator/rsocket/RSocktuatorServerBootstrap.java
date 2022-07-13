package socktuator.rsocket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServer.Transport;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.codec.ResourceEncoder;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

import io.rsocket.SocketAcceptor;
import socktuator.config.RSocktuatorProperties;
import socktuator.config.RSocktuatorProperties.Server;
import socktuator.discovery.SocktuatorOperationRegistry;

/**
 * A 'self-contained' RSocket server wrapper. This needs to be 'self contained' 
 * in the sense that it cannot be an actual RSocketServer, or a `@Controller`
 * or really any type of thing that the Spring Messaging / Spring Boot / RSocket
 * infrastructure would automatically pick up on and wire into the user's own
 * RSocket server setup. 
 * 
 * We can still try to leverage the frameworks machinery internal to this class/bean
 * as long as we don't actually create any beans that trigger/disable or interfere
 * in any way with Spring (Boot) RSocket autoconfig.
 * 
 * @author Kris De Volder (copied and adapted for RSocktuatorServer)
 */
public class RSocktuatorServerBootstrap implements SmartLifecycle, DisposableBean {
	
	//This class inspired by org.springframework.boot.rsocket.context.RSocketServerBootstrap
	//Essentially this copies the class and all its injected dependencies, to make for
	//a 'self contained' Bootstrap of RSocketServer that is independent of the hosting
	//application's own RSocketServer.

	private final List<RSocketServer> servers = new ArrayList<>();
	private ReactorResourceFactory ownedResourceFactory;

	public RSocktuatorServerBootstrap(
			RSocktuatorProperties rsocktuatorProps, 
			SocktuatorOperationRegistry operations,
			Optional<ReactorResourceFactory> _resourceFactory //Note: breaking the 'self containment' for ReactorResourceFactory.
													// This is deliberate, I think we want to share these 'low-level' reactor
			                                        // resources (i.e. borrowing thread and connection pools and the like from
													// the hosting application. I am hoping this won't cause any issues.
	) throws UnknownHostException {
		Map<String, Server> serversProps = rsocktuatorProps.getServers();
		for (Server serverProps : serversProps.values()) {
			// Perform a 'self contained' initialization op the server instance using only information from RSocktuatorXXX beans
			// This 'self-containedness' is meant to keep it so that RSocktuator config remains independent of host application's own
			// RSocketServer (if it has any).
			ReactorResourceFactory resourceFactory = _resourceFactory.orElseGet(this::createOwnedResourceFactory);
			RSocketServerFactory serverFactory = rSocketServerFactory(serverProps, resourceFactory);
			RSocketStrategies rSocketStrategies = rSocketStrategies();
//			List<RSocketMessageHandlerCustomizer> messageHandlerCustomizers = List.of();
			SocketAcceptor socketAcceptor = messageHandler(rSocketStrategies, operations);		
			this.servers.add(serverFactory.create(socketAcceptor));
		}
		
	}
	
	private RSocketStrategies rSocketStrategies() {
		RSocketStrategies.Builder builder = RSocketStrategies.builder();
//		if (ClassUtils.isPresent(PATHPATTERN_ROUTEMATCHER_CLASS, null)) {
//			builder.routeMatcher(new PathPatternRouteMatcher());
//		}
		
		builder.encoder(new ResourceEncoder());
		builder.encoder(new Jackson2JsonEncoder());
		builder.decoder(new Jackson2JsonDecoder());
		return builder.build();
	}

	private SocketAcceptor messageHandler(
			RSocketStrategies rSocketStrategies,
			SocktuatorOperationRegistry operations
	) {
		return RSocketMessageHandler.responder(rSocketStrategies, new RSocktuatorController(operations, rSocketStrategies));
	}

	private RSocketServerFactory rSocketServerFactory(
			RSocktuatorProperties.Server properties, 
			ReactorResourceFactory resourceFactory
	) throws UnknownHostException {
		NettyRSocketServerFactory factory = new NettyRSocketServerFactory();
		factory.setResourceFactory(resourceFactory);
		factory.setTransport(Transport.TCP);
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		String bindHost = properties.getHost();
		if (bindHost!=null) {
			factory.setAddress(InetAddress.getByName(bindHost));
		}
		map.from(properties.getPort()).to(factory::setPort);
//TODO:		map.from(properties.getFragmentSize()).to(factory::setFragmentSize);
//TODO:		map.from(properties.getSsl()).to(factory::setSsl);
		return factory;
	}
	

	@Override
	public void start() {
		for (RSocketServer server : servers) {
			server.start();
		}
	}

	@Override
	public void stop() {
		try {
			for (RSocketServer server : servers) {
				server.stop();
			}
		} finally {
			servers.clear();
		}
	}

	@Override
	public boolean isRunning() {
		return servers.stream().anyMatch(server -> server.address()!=null);
	}

	private ReactorResourceFactory createOwnedResourceFactory() {
		if (this.ownedResourceFactory==null) {
			this.ownedResourceFactory = new ReactorResourceFactory();
			this.ownedResourceFactory.afterPropertiesSet();
		}
		return this.ownedResourceFactory;
	}

	@Override
	public void destroy() throws Exception {
		if (this.ownedResourceFactory!=null) {
			this.ownedResourceFactory.destroy();
		}
	}
}
