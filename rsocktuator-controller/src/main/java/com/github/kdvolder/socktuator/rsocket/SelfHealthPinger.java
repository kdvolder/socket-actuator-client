package com.github.kdvolder.socktuator.rsocket;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import socktuator.config.RSocktuatorServerProps;
import socktuator.rsocket.RSocktuatorClient;

@Component
public class SelfHealthPinger {

	RSocktuatorClient client;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	public SelfHealthPinger(RSocktuatorServerProps props) {
		client = new RSocktuatorClient(props);
	}
	
	@Scheduled(fixedRate = 5000)
	void selfHealthCheck() throws Exception {
		Object h = client.health().block();
		System.out.println("Health = "+mapper.writeValueAsString(h));
//
//		Object ph = client.healthForPath("ping");
//		System.out.println("Health.ping = "+mapper.writeValueAsString(ph));
//		
//		OperationMetadata[] md = client.getEndpointMetadata();
//		System.out.println("ops = "+mapper.writeValueAsString(md));
		
//		Object metric = client.call("metrics.metric", Map.of(
//				"requiredMetricName", "jvm.memory.used",
//				"tag", List.of("area:heap")
//		));
//		System.out.println("metric = "+mapper.writeValueAsString(metric));

	}
	
}
