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
import socktuator.config.RSocktuatorProperties;
import socktuator.config.RSocktuatorProperties.Server;
import socktuator.config.SocktuatorServerProperties;
import socktuator.rsocket.RSocktuatorClient;

@Component
public class SelfHealthPinger {

	SocktuatorClient client;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	
	/**
	 * @param rsocketProps
	 * @param plainSocket
	 */
	public SelfHealthPinger(RSocktuatorProperties rsocketProps, SocktuatorServerProperties plainSocket) {
		// client = new SimpleSocketClient(
		// 		new InetSocketAddress(plainSocket.getHost(), plainSocket.getPort()),
		// 		Duration.ofMillis(plainSocket.getTimeout())
		// );
		Server someServer = rsocketProps.getServer().values().iterator().next();
		client = new RSocktuatorClient(
				new InetSocketAddress(someServer.getHost(), someServer.getPort()),
				someServer.getTimeout()
		);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	void selfHealthCheck() throws Exception {
		
		Mono.empty()
		.then(
				client.healthForPath("ping")
				.flatMap(printAsJson("health.ping"))
		).then(
				client.getEndpointMetadata()
				.flatMap(printAsJson("ops"))
		).then(
				client.health()
				.flatMap(printAsJson("health"))
		).then(
				client.call("metrics.metric", Map.of(
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
