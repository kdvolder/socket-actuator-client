package socktuator.endpoint.heap;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

@WebEndpoint(id="heap")
public class SocktuatorHeapEndpoint {
	
	@ReadOperation
	String dump() {
		return "This is a placeholder. We shall implement this later";
	}

}
