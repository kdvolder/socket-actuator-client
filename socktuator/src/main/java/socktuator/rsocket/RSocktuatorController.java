package socktuator.rsocket;

import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import reactor.core.publisher.Mono;
import socktuator.discovery.SocktuatorOperation;
import socktuator.discovery.SocktuatorOperationRegistry;
import socktuator.dto.Request;

@Controller
public class RSocktuatorController {
	
	private SocktuatorOperationRegistry operations;

	public RSocktuatorController(SocktuatorOperationRegistry operations) {
		this.operations = operations;
	}
	
	@MessageMapping(RSocktuatorRoutes.ACTUATOR)
	public Mono<Object> handleRequest(@Payload Request req) {
		return Mono.fromCallable(() -> {
			SocktuatorOperation op = operations.get(req.getOp());
			return op.invoke(new InvocationContext(SecurityContext.NONE, req.getParams()));
		});
	}
}
