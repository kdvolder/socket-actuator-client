package com.example.demo;

import java.net.InetSocketAddress;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.actuator.rsc.SimpleSocketClient;
import com.example.demo.actuator.rsc.SocktuatorServerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class SelfHealthPinger {

	SimpleSocketClient client;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	public SelfHealthPinger(SocktuatorServerProperties props) {
		client = new SimpleSocketClient(
				new InetSocketAddress(props.getHost(), props.getPort()),
				props.getTimeout()
		);
	}
	
	@Scheduled(fixedRate = 5000)
	void selfHealthCheck() throws Exception {
		Object h = client.health();
		System.out.println("Health = "+mapper.writeValueAsString(h));

		Object ph = client.healthForPath("ping");
		System.out.println("Health.ping = "+mapper.writeValueAsString(ph));
	}
	
}
