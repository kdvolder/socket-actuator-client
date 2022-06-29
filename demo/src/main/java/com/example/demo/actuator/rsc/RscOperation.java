package com.example.demo.actuator.rsc;

import org.springframework.boot.actuate.endpoint.Operation;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameters;

public interface RscOperation extends Operation {
	String getName();
	
	Class<?> getOutputType();
	OperationParameters getParameters();
	
}
