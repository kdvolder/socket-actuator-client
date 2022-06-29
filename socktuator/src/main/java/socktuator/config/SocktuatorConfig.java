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
import socktuator.server.SimpleSocketServer;

@Configuration
@EnableConfigurationProperties(SocktuatorServerProperties.class)
public class SocktuatorConfig {
	
	private ApplicationContext applicationContext;

	public SocktuatorConfig(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}	
	
	@ConditionalOnProperty(name = "socktuator.server.enabled")
	@Bean
	SimpleSocketServer socketActuatorServer(SocktuatorEndpointsSupplier endpoints, SocktuatorServerProperties props) {
		return new SimpleSocketServer(endpoints, props);
	}
	
	
	@Bean
	public SocktuatorEndpointDiscoverer rscAnnotationEndpointDiscoverer(ParameterValueMapper parameterValueMapper,
			ObjectProvider<OperationInvokerAdvisor> invokerAdvisors,
			ObjectProvider<EndpointFilter<ExposableSocktuatorEndpoint>> filters) {
		return new SocktuatorEndpointDiscoverer(this.applicationContext, parameterValueMapper,
				invokerAdvisors.orderedStream().collect(Collectors.toList()),
				filters.orderedStream().collect(Collectors.toList()));
	}

}
