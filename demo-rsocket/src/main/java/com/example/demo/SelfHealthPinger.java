package com.example.demo;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import reactor.core.publisher.Mono;
import socktuator.config.RSocktuatorServerProperties;
import socktuator.rsocket.RSocktuatorClient;

@Component
public class SelfHealthPinger {

	RSocktuatorClient client;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	public SelfHealthPinger(RSocktuatorServerProperties props) {
//		client = new SimpleSocketClient(
//				new InetSocketAddress(props.getHost(), props.getPort()),
//				props.getTimeout()
//		);
		client = new RSocktuatorClient(props);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	void selfHealthCheck() throws Exception {
		
		
		
//		Object h = client.health();
//		System.out.println("Health = "+mapper.writeValueAsString(h));

		client.healthForPath("ping").flatMap(printAsJson("health.ping"))
		.then(client.getEndpointMetadata().flatMap(printAsJson("ops")))
		.then(Mono.delay(Duration.ofSeconds(5)))
		.repeat()
		.subscribe();
		
		;
//		System.out.println("ops = "+mapper.writeValueAsString(md));
//		
//		Object metric = client.call("metrics.metric", Map.of(
//				"requiredMetricName", "jvm.memory.used",
//				"tag", List.of("area:heap")
//		));
//		System.out.println("metric = "+mapper.writeValueAsString(metric));

	}

	private Function<Object, Mono<Void>> printAsJson(String prefix) {
		return data -> Mono.fromCallable(() -> {
			System.out.println(prefix+" = "+mapper.writeValueAsString(data));
			return "ok";
		})
		.then();
	}
	
}
