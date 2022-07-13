package socktuator.endpoints;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.core.io.Resource;

import socktuator.endpoint.annot.SocktuatorEndpoint;


@SocktuatorEndpoint(id="heap")
public class SocktuatorHeapEndpoint {

	HeapDumpWebEndpoint heapDumper = new HeapDumpWebEndpoint();
	
	@ReadOperation
	public WebEndpointResponse<Resource> dump(Boolean live) {
		return heapDumper.heapDump(live);
	}

}
