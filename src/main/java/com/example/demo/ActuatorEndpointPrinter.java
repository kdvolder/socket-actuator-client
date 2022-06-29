package com.example.demo;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.demo.actuator.rsc.ExposableRscEndpoint;
import com.example.demo.actuator.rsc.RscEndpointDiscoverer;

@Component
public class ActuatorEndpointPrinter implements ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(ActuatorEndpointPrinter.class);
	
	private ApplicationContext context;
	
	@Autowired
	RscEndpointDiscoverer endpointsDiscoverer;
	
	@SuppressWarnings("rawtypes")
	@EventListener({ApplicationReadyEvent.class})
	void onReady() {
		log.info("READY!");
		System.out.println("=======================================");
		System.out.println(endpointsDiscoverer.getClass().getName());
		System.out.println("---------------------------------------");
		Collection<ExposableRscEndpoint> eps = endpointsDiscoverer.getEndpoints();
		for (ExposableRscEndpoint ep : eps) {
			System.out.println(ep);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

}
