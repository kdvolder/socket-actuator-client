package com.example.demo;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;

import socktuator.api.SocktuatorClient;

@Component
public class SampleResourceDumper {

	private static final Logger log = LoggerFactory.getLogger(SampleResourceDumper.class);

	@Autowired
	SocktuatorClient client;
	
	@EventListener(ApplicationReadyEvent.class)
	void dumpit() throws Exception {
		DataBufferUtils.join(
				client.callForBytes("resource.get", Map.of("location", "/sample.txt"))
		)
		.subscribe(
			result -> {
				System.out.println("Received data:");
				System.out.println("-----------------");
				System.out.println(result.toString(StandardCharsets.UTF_8));
				System.out.println("-----------------");
			}, 
			error -> {
				log.error("",error);
			});
	}


}
