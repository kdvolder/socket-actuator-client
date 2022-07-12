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
import socktuator.socket.SimpleSocketServerBootstrap;

@Configuration
@EnableConfigurationProperties({
	SocktuatorServerProperties.class,
	RSocktuatorProperties.class
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

	//TODO: attach an appropriate 'ConditionalOn', and support autoconfiguration.
	@Bean
	SimpleSocketServerBootstrap socketServerBootstrap(SocktuatorServerProperties props, SocktuatorOperationRegistry endpoints) {
		return new SimpleSocketServerBootstrap(props, endpoints); 
	}
	
	@Bean
	RSocktuatorServerBootstrap rsocktuatorServer(
			RSocktuatorProperties props, 
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
