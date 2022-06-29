package socktuator.debug;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import socktuator.discovery.ExposableRscEndpoint;
import socktuator.discovery.SocktuatorEndpointDiscoverer;

@Component
public class ActuatorEndpointPrinter {

	private static final Logger log = LoggerFactory.getLogger(ActuatorEndpointPrinter.class);
	
	@Autowired
	SocktuatorEndpointDiscoverer endpointsDiscoverer;
	
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
}
