package socktuator.rsocket;

import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.core.codec.ResourceEncoder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Controller;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import socktuator.discovery.SocktuatorOperation;
import socktuator.discovery.SocktuatorOperationRegistry;
import socktuator.dto.Request;

@Controller
public class RSocktuatorController {
	
	//TODO: instead of creating a @Controller class of our own, could we instead somehow use Spring messagging framework
	// classes to build up a MessageHandler / RSocket Acceptor that directly uses the handler methods (i.e. the methods 
	// that are annotated with @EndPoint). My initial exploration into doing this was inconclusive.
	//   - on the one hand the whole framework (See RSocketMessageHandler and its superclasses) seems to be pretty much
	//     built for the explicit and specific purpose of interpreting @MessageMapping annotated methods. 
	//     Since @Endpoint isn't a different annotation, it seems this is not possible.
	//   - however, it might be possible to still use the class somehow and make it work correclty for @Endpoint methods too.
	//     I don't understand how it all works, and it is complex, so it is hard to say.
	// For the time being we are 'working around' this bu defining different endpoints for returning different types of
	// data (this is where we need to have the framework handle the returned data in a data-type-specific manner. 
	// From what I have explored and understood so far, this handler logic uses reflection to decide behavior based on
	// the return type of a method (rather than the actual run-time type of the returned object).
	// So this means we have to create a different handler method here for each different return type we want to support.
	
	private SocktuatorOperationRegistry operations;
	private DataBufferFactory dataBufferFactory;

	public RSocktuatorController(SocktuatorOperationRegistry operations, RSocketStrategies rsocketStrategies) {
		this.operations = operations;
		this.dataBufferFactory = rsocketStrategies.dataBufferFactory();
	}
	
	@MessageMapping(RSocktuatorRoutes.ACTUATOR_JSON_SINGLE)
	public Mono<Object> handleRequest(@Payload Request req) {
		SocktuatorOperation op = operations.get(req.getOp());
		if (op==null) {
			Mono.error(new UnsupportedOperationException("Socktuator operation not found: '"+req.getOp()+"'"));
		}
		return Mono.fromCallable(() -> {
			try {
				return op.invoke(new InvocationContext(SecurityContext.NONE, req.getParams()));
			} catch (Throwable e) {
				e.printStackTrace();
				throw e;
			}
		});
	}
	
	@MessageMapping(RSocktuatorRoutes.ACTUATOR_BYTE_STREAM)
	public Flux<DataBuffer> handleRequestForBytes(@Payload Request req) {
		SocktuatorOperation op = operations.get(req.getOp());
		if (op==null) {
			return Flux.error(new UnsupportedOperationException("Socktuator operation not found: '"+req.getOp()+"'"));
		}
		//TODO: Perhaps here we could  (should!) borrow the framework machinery for handling returned data serialization. 
		// instead doing something custom for Resource only.
		if (Resource.class.isAssignableFrom(op.getOutputType())) {
			return Mono.fromCallable(() -> op.invoke(new InvocationContext(SecurityContext.NONE, req.getParams())))
					.cast(Resource.class)
					.flatMapMany(r -> DataBufferUtils.read(r, dataBufferFactory, ResourceEncoder.DEFAULT_BUFFER_SIZE));
		} else {
			throw new UnsupportedOperationException(RSocktuatorRoutes.ACTUATOR_BYTE_STREAM+" only supports operations that return 'Resource'.\n" +
					"The requested operation "+op.getName()+" returns "+op.getOutputType().getCanonicalName()
			);
		}
		
	}
	
}
