package socktuator.discovery;

import org.springframework.boot.actuate.endpoint.EndpointsSupplier;

@FunctionalInterface
public interface RscEndpointsSupplier extends EndpointsSupplier<ExposableRscEndpoint> {

}
