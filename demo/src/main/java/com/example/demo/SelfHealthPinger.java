package com.example.demo;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import socktuator.config.SocktuatorSocketServerProperties;
import socktuator.dto.OperationMetadata;
import socktuator.socket.SimpleSocketClient;

@Component
public class SelfHealthPinger {

	SimpleSocketClient client;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	public SelfHealthPinger(SocktuatorSocketServerProperties props) {
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
		
		OperationMetadata[] md = client.getEndpointMetadata();
		System.out.println("ops = "+mapper.writeValueAsString(md));
		
		Object metric = client.call("metrics.metric", Map.of(
				"requiredMetricName", "jvm.memory.used",
				"tag", List.of("area:heap")
		));
		System.out.println("metric = "+mapper.writeValueAsString(metric));

	}
	
}
