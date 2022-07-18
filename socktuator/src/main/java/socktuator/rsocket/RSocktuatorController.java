package socktuator.rsocket;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import socktuator.discovery.SocktuatorOperation;
import socktuator.discovery.SocktuatorOperationParameter;
import socktuator.discovery.SocktuatorOperationRegistry;
import socktuator.dto.Request;

@Controller
public class RSocktuatorController {
	
	private static final Logger log = LoggerFactory.getLogger(RSocktuatorController.class);
	
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
	private RSocketStrategies strategies;

	public RSocktuatorController(SocktuatorOperationRegistry operations, RSocketStrategies rsocketStrategies) {
		this.operations = operations;
		this.strategies = rsocketStrategies;
	}
	
	@MessageMapping(RSocktuatorRoutes.ACTUATOR_JSON_SINGLE)
	public Mono<Object> handleRequest(@Payload Request req) {
		SocktuatorOperation op = operations.get(req.getOp());
		if (op==null) {
			Mono.error(new UnsupportedOperationException("Socktuator operation not found: '"+req.getOp()+"'"));
		}
		return Mono.fromCallable(() -> {
			try {
				return op.invoke(createInvocationContext(req, op));
			} catch (Throwable e) {
				e.printStackTrace();
				throw e;
			}
		});
	}
	
	protected InvocationContext createInvocationContext(Request req, SocktuatorOperation op) {
		HashMap<String, Object> paramValues = new HashMap<>(req.getParams());
		for (SocktuatorOperationParameter formalParam : op.getParameters()) {
			if (formalParam.isMandatory() && !paramValues.containsKey(formalParam.getName())) {
				//missing required param. Let's supply a default if it makes sense
				if (formalParam.getType().equals(boolean.class) || formalParam.getType().equals(Boolean.class)) {
					paramValues.put(formalParam.getName(), false);
				}
			}
		}
		return new InvocationContext(SecurityContext.NONE, paramValues);
	}
	
//	@MessageMapping(RSocktuatorRoutes.ACTUATOR_BYTE_STREAM)
//	public Flux<DataBuffer> handleRequestForBytes(@Payload Request req) {
//		SocktuatorOperation op = operations.get(req.getOp());
//		if (op==null) {
//			return Flux.error(new UnsupportedOperationException("Socktuator operation not found: '"+req.getOp()+"'"));
//		}
//		//TODO: Perhaps here we could  (should!) borrow the framework machinery for handling returned data serialization. 
//		// instead doing something custom for Resource only.
//		if (Resource.class.isAssignableFrom(op.getOutputType())) {
//			return Mono.fromCallable(() -> op.invoke(createInvocationContext(req, op)))
//					.cast(Resource.class)
//					.flatMapMany(r -> DataBufferUtils.read(r, strategies.dataBufferFactory(), ResourceEncoder.DEFAULT_BUFFER_SIZE));
//		} else {
//			throw new UnsupportedOperationException(RSocktuatorRoutes.ACTUATOR_BYTE_STREAM+" only supports operations that return 'Resource'.\n" +
//					"The requested operation "+op.getName()+" returns "+op.getOutputType().getCanonicalName()
//			);
//		}
//		
//	}
	
	@MessageMapping(RSocktuatorRoutes.ACTUATOR_BYTE_STREAM)
	public Flux<DataBuffer> handleRequestForBytes(@Payload Request req) {
		String _mimeType = req.getMimeType();
		if (_mimeType==null) {
			return Flux.error(new IllegalArgumentException("mimeType is required but not supplied"));
		}
		MimeType mimeType = MimeType.valueOf(_mimeType);
		SocktuatorOperation op = operations.get(req.getOp());
		if (op==null) {
			return Flux.error(new UnsupportedOperationException("Socktuator operation not found: '"+req.getOp()+"'"));
		}
		Encoder<Object> encoder = strategies.encoder(ResolvableType.forClass(op.getOutputType()), mimeType);
		if (encoder==null) {
			return Flux.error(new UnsupportedOperationException("No encoder found converting '"+op.getOutputType().getSimpleName()+"' to '"+mimeType+"'"));
		}
		Mono<Object> result = Mono.fromCallable(() -> op.invoke(createInvocationContext(req, op)));
		return encoder.encode(result, strategies.dataBufferFactory(), ResolvableType.forClass(op.getOutputType()), mimeType, null)
		.doOnError(e -> {
					log.error("Problem", e);		
		});
	}
}
