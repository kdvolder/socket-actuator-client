package socktuator.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Provides a shared instance of jackson ObjectMapper configured in
 * a very specific way to facilitate communication between Socktuator
 * client and server using the same, consistent configuration.
 * 
 * @author Kris De Volder
 */
public class SharedObjectMapper {
	
	private static ObjectMapper instance;
	
	public static synchronized ObjectMapper get() {
		if (instance==null) {
			instance = new ObjectMapper();
			instance.registerModule(new JavaTimeModule());
		}
		return instance;
	}

}
