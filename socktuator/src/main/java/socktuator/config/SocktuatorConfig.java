package socktuator.config;

import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;

import socktuator.debug.ActuatorEndpointPrinter;
import socktuator.discovery.ExposableSocktuatorEndpoint;
import socktuator.discovery.SocktuatorEndpointDiscoverer;
import socktuator.discovery.SocktuatorEndpointsSupplier;
import socktuator.discovery.SocktuatorOperationRegistry;
import socktuator.endpoints.ResourceEndpoint;
import socktuator.endpoints.SocktuatorHeapEndpoint;
import socktuator.rsocket.RSocktuatorServerBootstrap;
import socktuator.socket.SimpleSocketServerBootstrap;
import socktuator.web.SocktuatorWebBootstrap;

@Configuration
@EnableConfigurationProperties({
	SocktuatorServerProperties.class,
	RSocktuatorProperties.class,
	SocktuatorWebProperties.class
})
@ComponentScan(basePackageClasses = {
		socktuator.endpoints.ResourceEndpoint.class,
		ActuatorEndpointPrinter.class
})
public class SocktuatorConfig {
	
	private ApplicationContext applicationContext;

	public SocktuatorConfig(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}	
	
	@Bean
	SocktuatorHeapEndpoint heapDumpEndpoint() {
		return new SocktuatorHeapEndpoint();
	}
	
	@Bean
	ResourceEndpoint resourceEndpoint(ApplicationContext ctx) {
		return new ResourceEndpoint(ctx);
	}
	
	@Bean
	SocktuatorOperationRegistry socktuatorOperations(SocktuatorEndpointsSupplier endpoints) {
		return new SocktuatorOperationRegistry(endpoints);
	}

	//TODO: attach an appropriate 'ConditionalOn', and support autoconfiguration.
	@Bean
	SimpleSocketServerBootstrap socktuatorServerBootstrap(SocktuatorServerProperties props, SocktuatorOperationRegistry endpoints) {
		return new SimpleSocketServerBootstrap(props, endpoints); 
	}
	
	//TODO: attach an appropriate 'ConditionalOn', and support autoconfiguration.
	@Bean
	RSocktuatorServerBootstrap rsocktuatorServerBootstrap(
			RSocktuatorProperties props, 
			Optional<ReactorResourceFactory> resourceFactory, 
			SocktuatorOperationRegistry operations
	) throws UnknownHostException {
		return new RSocktuatorServerBootstrap(props, operations, resourceFactory);
	}

    @Bean
    SocktuatorEndpointDiscoverer socktuatorAnnotationEndpointDiscoverer(ParameterValueMapper parameterValueMapper,
            ObjectProvider<OperationInvokerAdvisor> invokerAdvisors,
            ObjectProvider<EndpointFilter<ExposableSocktuatorEndpoint>> filters) {
        return new SocktuatorEndpointDiscoverer(this.applicationContext, parameterValueMapper,
                invokerAdvisors.orderedStream().collect(Collectors.toList()),
                filters.orderedStream().collect(Collectors.toList()));
    }
    
	//TODO: attach an appropriate 'ConditionalOn', and support autoconfiguration.
    @Bean
	SocktuatorWebBootstrap sideApplication(ApplicationContext hostAppCtx, SocktuatorWebProperties props) {
		return new SocktuatorWebBootstrap(hostAppCtx, props);
	}
}
