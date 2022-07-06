package socktuator.dto;

import java.util.Map;

public class Request {
	String op;
	Map<String, Object> params;
	public Request() {}
	public Request(String op, Map<String, Object>  params) {
		this.op = op;
		this.params = params;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
}