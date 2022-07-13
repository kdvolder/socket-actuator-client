package com.example.demo;

import java.net.InetSocketAddress;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import socktuator.api.SocktuatorClient;
import socktuator.config.RSocktuatorProperties;
import socktuator.rsocket.RSocktuatorClient;

@Configuration
public class SocktuatorClientConfig {

	@Bean
	SocktuatorClient socktuatorClient(RSocktuatorProperties rsocketProps) {
//		return new SimpleSocketClient(
//		 		new InetSocketAddress(someServer.getHost(), someServer.getPort()),
//		 		Duration.ofMillis(someServer.getTimeout())
//		);
		 
		RSocktuatorProperties.Server someServer = rsocketProps.getServers().values().iterator().next();
		return new RSocktuatorClient(
				new InetSocketAddress(someServer.getHost(), someServer.getPort()),
				someServer.getTimeout()
		);
	}
}
