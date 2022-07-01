package socktuator.discovery;

import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameter;

public interface SocktuatorOperationParameter extends OperationParameter {

	public Selector.Match getPathParam();
	
}
