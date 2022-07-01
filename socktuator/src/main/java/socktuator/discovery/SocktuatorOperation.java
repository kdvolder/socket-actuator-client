package socktuator.discovery;

import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.Operation;

public interface SocktuatorOperation extends Operation {
	EndpointId getEndpoint();
	String getName();	
	Class<?> getOutputType();
	SocktuatorOperationParameter[] getParameters();
	
}
