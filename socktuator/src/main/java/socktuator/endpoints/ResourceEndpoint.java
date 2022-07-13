package socktuator.endpoints;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import socktuator.endpoint.annot.SocktuatorEndpoint;

@SocktuatorEndpoint(id="resource")
public class ResourceEndpoint {
	
	//TODO: Remove: This endpoint is for demo and testing purposes, consider removing it later.
	
	ApplicationContext ctx;
	
	public ResourceEndpoint(ApplicationContext ctx) {
		this.ctx = ctx;
	}
	
	@ReadOperation
	public Resource get(String location) throws IOException {
		ClassPathResource test = new ClassPathResource(location);
		try (InputStream input = test.getInputStream()) {
			System.out.println(IOUtils.toString(input, StandardCharsets.UTF_8));
		}
		return test;
	}

}
