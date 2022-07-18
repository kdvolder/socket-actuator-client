package socktuator.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.invoke.convert.ConversionServiceParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import socktuator.config.SocktuatorWebProperties;
import socktuator.config.SocktuatorWebProperties.WebServerProps;

/**
 * Start up a 'independent' application context that consumes unfiltered WebEndpoint
 */
public class SocktuatorWebBootstrap implements SmartLifecycle {
	
	//TODO: At present this is limited to a single 'instance' so not multiple independently configured ones.
	
	private List<AnnotationConfigServletWebServerApplicationContext> contexts;
	
	public SocktuatorWebBootstrap(ApplicationContext hostAppCtx, SocktuatorWebProperties props) {
		if (props.getServers().isEmpty()) {
			contexts = List.of();
		} else {
			contexts = new ArrayList<>(props.getServers().size());
			WebEndpointDiscoverer unfilteredEndpoints = new WebEndpointDiscoverer(hostAppCtx, 
					new ConversionServiceParameterValueMapper(ApplicationConversionService.getSharedInstance()),
					EndpointMediaTypes.DEFAULT,
					List.of(), List.of(), List.of()
			);

			for (Entry<String, WebServerProps> entry : props.getServers().entrySet()) {
				String stakeholderKey = entry.getKey();
				WebServerProps serverProps = entry.getValue();
				AnnotationConfigServletWebServerApplicationContext ctx = new AnnotationConfigServletWebServerApplicationContext();
				ctx.setServerNamespace(stakeholderKey);
				
				//Some beans are explicitly 'shared' with the hostApp context. Anything else is 'independent'.
				ctx.registerBean("socktuatorWebProperties", WebServerProps.class, () -> serverProps);
				ctx.registerBean("webEndpoints", WebEndpointDiscoverer.class, () -> unfilteredEndpoints);
				
				//Beans below are not shared, but instantiated by the ctx.
				ctx.register(WebtuatorConfig.class);
				contexts.add(ctx);
			}
		}
		
		
	}
	
	@Configuration
	@Import({ ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class })
	@EnableWebMvc
	static class WebtuatorConfig {
				
		@Autowired
		WebEndpointDiscoverer webEndpoints;
		
		@Autowired
		Environment env;
		
		@Autowired
		SocktuatorWebProperties.WebServerProps props;
		
		@EventListener
		void onContextStarted(ContextStartedEvent evt) {
			//TODO: Remove this... only debugging code
			
			for (ExposableWebEndpoint ep : webEndpoints.getEndpoints()) {
				System.out.println(ep);
			}
		}
		
		
		@Bean
		TomcatServletWebServerFactory tomcatFactory() {
			return new TomcatServletWebServerFactory();
		}
		
		@Bean
		WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatPortCustomizer() {
			//Note: this is commented out because it doesn't work
			// Presmably we are missing some kind of bean (or postProcessor) that is responsible
			// for finding WebServerFactoryCustomizer and applying them.
			// probably it is this: org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar
			return f -> {
				f.setPort(props.getPort());
			};
		}
		
		@Bean
		DispatcherServlet dispatcherServlet() {
			return new DispatcherServlet();
		}
		
        @Bean
        WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier
//				ServletEndpointsSupplier servletEndpointsSupplier, 
//				ControllerEndpointsSupplier controllerEndpointsSupplier,
//				EndpointMediaTypes endpointMediaTypes, 
//                CorsEndpointProperties corsProperties
//				WebEndpointProperties webEndpointProperties
//				Environment environment
        ) {
            List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
            Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
            allEndpoints.addAll(webEndpoints);
//			allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
//			allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
            String basePath = "/";
            EndpointMapping endpointMapping = new EndpointMapping(basePath);
            boolean shouldRegisterLinksMapping = true;
            return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, EndpointMediaTypes.DEFAULT,
                    null, new EndpointLinksResolver(allEndpoints, basePath),
                    shouldRegisterLinksMapping, WebMvcAutoConfiguration.pathPatternParser);
        }
		
		
	}
	

	@Override
	public void start() {
		for (AnnotationConfigServletWebServerApplicationContext ctx : contexts) {
			ctx.refresh();
			ctx.start();
		}
	}

	@Override
	public void stop() {
		for (AnnotationConfigServletWebServerApplicationContext ctx : contexts) {
			ctx.stop();
		}
	}

	@Override
	public boolean isRunning() {
		for (AnnotationConfigServletWebServerApplicationContext ctx : contexts) {
			if (ctx.isRunning()) {
				return true;
			}
		}
		return false;
	}

}
