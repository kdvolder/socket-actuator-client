package com.example.demo.actuator.rsc;

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

@Configuration
@EnableConfigurationProperties(SocktuatorServerProperties.class)
public class RscActuatorConfig {
	
	private ApplicationContext applicationContext;

	public RscActuatorConfig(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}	
	
	@ConditionalOnProperty(name = "socktuator.server.enabled")
	@Bean
	SimpleSocketServer socketActuatorServer(RscEndpointsSupplier endpoints, SocktuatorServerProperties props) {
		return new SimpleSocketServer(endpoints, props);
	}
	
	
	@Bean
	public RscEndpointDiscoverer rscAnnotationEndpointDiscoverer(ParameterValueMapper parameterValueMapper,
			ObjectProvider<OperationInvokerAdvisor> invokerAdvisors,
			ObjectProvider<EndpointFilter<ExposableRscEndpoint>> filters) {
		return new RscEndpointDiscoverer(this.applicationContext, parameterValueMapper,
				invokerAdvisors.orderedStream().collect(Collectors.toList()),
				filters.orderedStream().collect(Collectors.toList()));
	}

}
