package com.example.demo;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import reactor.core.publisher.Mono;
import socktuator.api.SocktuatorClient;

@Component
public class SelfHealthPinger {

	@Autowired
	SocktuatorClient client;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	@EventListener(ApplicationReadyEvent.class)
	void selfHealthCheck() throws Exception {
		
		Mono.empty()
		.then(
				client.healthForPath("ping")
				.flatMap(printAsJson("health.ping")))
		.then(
				client.getEndpointMetadata()
				.flatMap(printAsJson("ops")))
		.then(
				client.health()
				.flatMap(printAsJson("health")))
		.then(
				client.call("metrics.metric", Map.of(
						"requiredMetricName", "jvm.memory.used",
						"tag", List.of("area:heap")
				))
				.flatMap(printAsJson("metric")))
		.then(
				Mono.delay(Duration.ofSeconds(5)))
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
