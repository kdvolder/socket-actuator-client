package com.example.demo;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import reactor.core.publisher.Mono;
import socktuator.api.SocktuatorClient;
import socktuator.config.RSocktuatorServerProperties;
import socktuator.rsocket.RSocktuatorClient;

@Component
public class SelfHealthPinger {

	SocktuatorClient client;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	public SelfHealthPinger(RSocktuatorServerProperties serverProps) {
//		client = new SimpleSocketClient(
//				new InetSocketAddress(props.getHost(), props.getPort()),
//				props.getTimeout()
//		);
		client = new RSocktuatorClient(
				new InetSocketAddress(serverProps.getHost(), serverProps.getPort()),
				serverProps.getTimeout()
		);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	void selfHealthCheck() throws Exception {
		
		Mono.empty()
		.then(
				client.healthForPath_mono("ping")
				.flatMap(printAsJson("health.ping"))
		).then(
				client.getEndpointMetadata()
				.flatMap(printAsJson("ops"))
		).then(
				client.health_mono()
				.flatMap(printAsJson("health"))
		).then(
				client.call_mono("metrics.metric", Map.of(
						"requiredMetricName", "jvm.memory.used",
						"tag", List.of("area:heap")
				))
				.flatMap(printAsJson("metric"))
		).then(
				Mono.delay(Duration.ofSeconds(5))
		)
		.repeat()
		.subscribe();
	}

	private Function<Object, Mono<Void>> printAsJson(String prefix) {
		return data -> Mono.fromCallable(() -> {
			System.out.println(prefix+" = "+mapper.writeValueAsString(data));
			return "ok";
		})
		.then();
	}
	
}
