package com.example.demo;

import java.net.InetSocketAddress;
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
import com.example.demo.actuator.rsc.SimpleSocketClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class SelfHealthPinger {

	SimpleSocketClient client = new SimpleSocketClient(new InetSocketAddress("localhost", 7007));

	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	@Scheduled(fixedRate = 5000)
	void selfHealthCheck() throws Exception {
		Object h = client.health();
		System.out.println("Health = "+mapper.writeValueAsString(h));
	}
	
}
