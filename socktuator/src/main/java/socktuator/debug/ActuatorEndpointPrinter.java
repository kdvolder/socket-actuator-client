package socktuator.debug;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import socktuator.discovery.ExposableSocktuatorEndpoint;
import socktuator.discovery.SocktuatorEndpointDiscoverer;

/**
 * A component that dumps out information about Actuator endpoints.
 * Mostly for debugging purposes.
 */
//@Component
public class ActuatorEndpointPrinter {

	@Autowired
	SocktuatorEndpointDiscoverer sockDiscoverer;
	
	@Autowired
	WebEndpointDiscoverer webDiscoverer;
	
	@EventListener({ApplicationReadyEvent.class})
	void onReady() {
		{
			System.out.println("=======================================");
			System.out.println(sockDiscoverer.getClass().getName());
			System.out.println("---------------------------------------");
			Collection<ExposableSocktuatorEndpoint> eps = sockDiscoverer.getEndpoints();
			for (ExposableSocktuatorEndpoint ep : eps) {
				System.out.println(ep);
			}
		}
		{
			System.out.println("=======================================");
			System.out.println(webDiscoverer.getClass().getName());
			System.out.println("---------------------------------------");
			Collection<ExposableWebEndpoint> eps = webDiscoverer.getEndpoints();
			for (ExposableWebEndpoint ep : eps) {
				System.out.println("endpoint = "+ep.getEndpointId());
				for (WebOperation op : ep.getOperations()) {
					System.out.println(ep.getEndpointId() +"." + op.getId() + " : "+op.getType());
					op.getRequestPredicate();
					System.out.println("  ");
				}
				System.out.println(ep);
			}
		}
		
	}
}
