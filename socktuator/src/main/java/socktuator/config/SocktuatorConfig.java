package socktuator.config;

import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;

import socktuator.discovery.ExposableSocktuatorEndpoint;
import socktuator.discovery.SocktuatorEndpointDiscoverer;
import socktuator.discovery.SocktuatorEndpointsSupplier;
import socktuator.discovery.SocktuatorOperationRegistry;
import socktuator.rsocket.RSocktuatorServerBootstrap;
import socktuator.socket.SimpleSocketServer;

@Configuration
@EnableConfigurationProperties({
	SocktuatorServerProperties.class,
	RSocktuatorServerProperties.class
})
public class SocktuatorConfig {
	
	private ApplicationContext applicationContext;

	public SocktuatorConfig(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}	
	
	@Bean
	SocktuatorOperationRegistry socktuatorOperations(SocktuatorEndpointsSupplier endpoints) {
		return new SocktuatorOperationRegistry(endpoints);
	}
	
	@ConditionalOnProperty(name = "socktuator.socket.server.enabled")
	@Bean
	SimpleSocketServer socketActuatorServer(SocktuatorServerProperties props, SocktuatorOperationRegistry endpoints) {
		return new SimpleSocketServer(props, endpoints); 
	}
	
	@ConditionalOnProperty(name = "socktuator.rsocket.server.enabled")
	@Bean
	RSocktuatorServerBootstrap rsocktuatorServer(
			RSocktuatorServerProperties props, 
			Optional<ReactorResourceFactory> resourceFactory, 
			SocktuatorOperationRegistry operations
	) throws UnknownHostException {
		return new RSocktuatorServerBootstrap(props, operations, resourceFactory);
	}

	@Bean
	public SocktuatorEndpointDiscoverer socktuatorAnnotationEndpointDiscoverer(ParameterValueMapper parameterValueMapper,
			ObjectProvider<OperationInvokerAdvisor> invokerAdvisors,
			ObjectProvider<EndpointFilter<ExposableSocktuatorEndpoint>> filters) {
		return new SocktuatorEndpointDiscoverer(this.applicationContext, parameterValueMapper,
				invokerAdvisors.orderedStream().collect(Collectors.toList()),
				filters.orderedStream().collect(Collectors.toList()));
	}

}
