package socktuator.util;

public class StringUtil {

	public static String removePrefix(String prefix, String s) {
		if (s.startsWith(prefix)) {
			return s.substring(prefix.length());
		}
		return s;
	}

}
