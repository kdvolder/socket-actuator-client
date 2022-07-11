package socktuator.rsocket;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import socktuator.api.SocktuatorClient;
import socktuator.dto.Request;

public class RSocktuatorClient implements SocktuatorClient {
	
	private static final Logger log = LoggerFactory.getLogger(RSocktuatorClient.class);
	
	private RSocketRequester requestor;
	private Duration so_timeout;

	public RSocktuatorClient(InetSocketAddress target, Duration so_timeout) {
		this.so_timeout = so_timeout;
		log.info("Creating RSocktuatorClient targetting {}:{}", target.getHostName(), target.getPort());
		RSocketRequester.Builder builder = RSocketRequester.builder();
        requestor = builder
          .rsocketConnector(
             rSocketConnector ->
               rSocketConnector.reconnect(Retry.indefinitely())
          )
          .rsocketStrategies(sb -> {
        	  sb.decoder(new Jackson2JsonDecoder());
        	  sb.encoder(new Jackson2JsonEncoder());
          })
          .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
          .tcp(target.getHostName(), target.getPort());
	}

	
	@Override
	public Mono<Object> call(String operationId, Map<String, Object> params) {
		return requestor
			.route(RSocktuatorRoutes.ACTUATOR)
			.data(new Request(operationId, params))
			.retrieveMono(Object.class)
			.timeout(so_timeout);
	}

}
