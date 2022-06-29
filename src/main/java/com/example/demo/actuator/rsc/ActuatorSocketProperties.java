package com.example.demo.actuator.rsc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.actuator.socket")
public class ActuatorSocketProperties {

	private boolean enabled = false;
	
	private int port = 7007;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
