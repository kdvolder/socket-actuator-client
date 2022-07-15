package socktuator.endpoints;

import java.io.IOException;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

import socktuator.endpoint.annot.SocktuatorEndpoint;


@SocktuatorEndpoint(id="socktuator.heapdump")
public class SocktuatorHeapEndpoint {

	HeapDumpWebEndpoint heapDumper = new HeapDumpWebEndpoint();
	
	@ReadOperation(produces = {MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE})
	public Resource dump(Boolean live) throws IOException {
		WebEndpointResponse<Resource> webResponse = heapDumper.heapDump(true);
		Resource resource = webResponse.getBody();
		if (resource!=null) {
			System.out.println("heapdump size "+resource.contentLength());
			return resource;
		} else {
			throw new IOException("No heap dump found. HttpStatus = "+webResponse.getStatus());
		}
	}

}
