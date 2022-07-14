package socktuator.discovery;

import java.util.List;

import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.Operation;

public interface SocktuatorOperation extends Operation {
	EndpointId getEndpoint();
	String getName();	
	Class<?> getOutputType();
	SocktuatorOperationParameter[] getParameters();
	List<String> getProduces();
	SocktuatorOperation processAlias();
	
}
