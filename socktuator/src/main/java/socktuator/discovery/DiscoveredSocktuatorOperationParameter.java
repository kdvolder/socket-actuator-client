package socktuator.discovery;

import java.lang.reflect.Parameter;

import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.Selector.Match;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameter;

public class DiscoveredSocktuatorOperationParameter implements SocktuatorOperationParameter {

	private OperationParameter operationParameter;
	private Parameter methodParameter;

	public DiscoveredSocktuatorOperationParameter(OperationParameter operationParameter, Parameter parameter) {
		this.operationParameter = operationParameter;
		this.methodParameter = parameter;
	}

	@Override
	public String getName() {
		return operationParameter.getName();
	}

	@Override
	public Class<?> getType() {
		return operationParameter.getType();
	}

	@Override
	public boolean isMandatory() {
		return operationParameter.isMandatory();
	}

	@Override
	public Match getPathParam() {
		Selector s = methodParameter.getAnnotation(Selector.class);
		return s==null ? null : s.match();
	}

	@Override
	public String toString() {
		return "SocktuatorOperationParameter("+getName()+ " : "+getType().getSimpleName()+")";
	}

	
	
}
