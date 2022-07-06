package com.github.kdvolder.socktuator.rsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import socktuator.config.SocktuatorConfig;

@SpringBootApplication
@Import(SocktuatorConfig.class)
public class RSocktuatorControllerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RSocktuatorControllerApplication.class, args);
	}

}
