package com.example.demo.actuator.rsc;

import org.springframework.boot.actuate.endpoint.EndpointsSupplier;

@FunctionalInterface
public interface RscEndpointsSupplier extends EndpointsSupplier<ExposableRscEndpoint> {

}
