package com.example.demo.actuator.rsc;

import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredOperation;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredOperationMethod;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameters;

public class DiscoveredRscOperation extends AbstractDiscoveredOperation implements RscOperation {

	private EndpointId endpointId;
	private DiscoveredOperationMethod operationMethod;

	public DiscoveredRscOperation(EndpointId endpointId, DiscoveredOperationMethod operationMethod, OperationInvoker invoker) {
		super(operationMethod, invoker);
		this.endpointId = endpointId;
		this.operationMethod = operationMethod;
	}

	@Override
	public String getName() {
		return endpointId.toString() + "." + operationMethod.getMethod().getName();
	}

	@Override
	public Class<?> getOutputType() {
		return operationMethod.getMethod().getReturnType();
	}

	@Override
	public OperationParameters getParameters() {
		return operationMethod.getParameters();
	}

}
