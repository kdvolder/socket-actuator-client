package socktuator.debug;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EndpointAnnotationFinding {

	//TODO: This class is just for debuggign / exploration. Remove it.

	@Autowired
	ApplicationContext ctx;
	
	@EventListener({ApplicationReadyEvent.class})
	void onReady() {
		String[] names = BeanFactoryUtils.beanNamesForAnnotationIncludingAncestors(this.ctx,
				Endpoint.class);
		System.out.println(">>> @Endpoint beans found:");
		for (String string : names) {
			System.out.println(string);
		}
		System.out.println("<<< @Endpoint beans found");
	}
	
}
