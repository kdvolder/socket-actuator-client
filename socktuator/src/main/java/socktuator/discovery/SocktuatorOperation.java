package socktuator.discovery;

import org.springframework.boot.actuate.endpoint.Operation;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameters;

public interface SocktuatorOperation extends Operation {
	String getName();
	
	Class<?> getOutputType();
	OperationParameters getParameters();
	
}
