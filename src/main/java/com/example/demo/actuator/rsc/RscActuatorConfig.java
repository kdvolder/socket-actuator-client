package com.example.demo.actuator.rsc;

import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.jmx.ExposableJmxEndpoint;
import org.springframework.boot.actuate.endpoint.jmx.annotation.JmxEndpointDiscoverer;
import org.springframework.boot.autoconfigure.jmx.JmxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ActuatorSocketProperties.class)
public class RscActuatorConfig {
	
	private ApplicationContext applicationContext;
	private JmxEndpointProperties properties;

	public RscActuatorConfig(ApplicationContext applicationContext, JmxEndpointProperties properties,
			JmxProperties jmxProperties) {
		this.applicationContext = applicationContext;
		this.properties = properties;
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
