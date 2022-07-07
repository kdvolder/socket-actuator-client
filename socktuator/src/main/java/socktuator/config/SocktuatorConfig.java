package socktuator.config;

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

import socktuator.discovery.ExposableSocktuatorEndpoint;
import socktuator.discovery.SocktuatorEndpointDiscoverer;
import socktuator.discovery.SocktuatorEndpointsSupplier;
import socktuator.discovery.SocktuatorOperationRegistry;
import socktuator.rsocket.RSocktuatorController;
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
	RSocktuatorController rsocketController(SocktuatorOperationRegistry ops) {
		return new RSocktuatorController(ops);
	}
	
	//TODO: consume the config props from RSoctuatorServerProps to create
	// an RSocket server by ourself (i.e. not using Spring Boot autoconfig) and
	// then define it precisely how we want.
	//I am not yet sure this is possible but a good place to start exploring is this
	// method: org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler.responder(RSocketStrategies, Object...)

	@Bean
	public SocktuatorEndpointDiscoverer socktuatorAnnotationEndpointDiscoverer(ParameterValueMapper parameterValueMapper,
			ObjectProvider<OperationInvokerAdvisor> invokerAdvisors,
			ObjectProvider<EndpointFilter<ExposableSocktuatorEndpoint>> filters) {
		return new SocktuatorEndpointDiscoverer(this.applicationContext, parameterValueMapper,
				invokerAdvisors.orderedStream().collect(Collectors.toList()),
				filters.orderedStream().collect(Collectors.toList()));
	}

}
