package socktuator.api;

import java.util.Map;

import reactor.core.publisher.Mono;
import socktuator.dto.OperationMetadata;
import socktuator.dto.SharedObjectMapper;

public interface SocktuatorClient {

	Mono<Object> call_mono(String operationId, Map<String, Object> params);
	
	default Mono<OperationMetadata[]> getEndpointMetadata() {
		return call_mono("actuator.actuator", Map.of())
		.map(untyped_resp -> SharedObjectMapper.get().convertValue(untyped_resp, OperationMetadata[].class));
	}

	default Mono<Object> health_mono() {
		return call_mono("health.health", Map.of());
	}
	
	default Mono<Object> healthForPath_mono(String... path) {
		return call_mono("health.healthForPath", Map.of("path", path));
	}
}
