package com.example.demo;

import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameters;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.example.demo.actuator.rsc.ExposableRscEndpoint;
import com.example.demo.actuator.rsc.RscEndpointDiscoverer;
import com.example.demo.actuator.rsc.RscOperation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SelfHealthPingerViaDsicorey {

	@Autowired
	RscEndpointDiscoverer epd;
	
	private RscOperation getOperation(String endpoint, String opName) {
		for (ExposableRscEndpoint ep : epd.getEndpoints()) {
			if (ep.getEndpointId().toString().equals(endpoint)) {
				for (RscOperation op : ep.getOperations()) {
					if (op.getName().equals(endpoint+"."+opName)) {
						return op;
					}
				}
			}
		}
		throw new NoSuchElementException("Couldn't find the requested operation");
	}
	
	@Scheduled(fixedRate = 15000)
	void selfHealthCheck() throws Exception {
		RscOperation op = getOperation("health", "health");
		Assert.isTrue(hasNoParameters(op), "Expecting an operation with no parameters but got "+op.getParameters());
		InvocationContext ctx = new InvocationContext(SecurityContext.NONE, new HashMap<>());
		Object result = op.invoke(ctx);
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		System.out.println("Health = "+mapper.writeValueAsString(result));
	}

	private boolean hasNoParameters(RscOperation op) {
		OperationParameters params = op.getParameters();
		return params.getParameterCount() == 0;
	}
	
}
