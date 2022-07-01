package socktuator.dto;

public class Response {
	private String error; // an error for a failed invocation
	private Object result; // the return value of a successful invocation
	public Response() {}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public static Response ok(Object result) { 
		Response self = new Response();
		self.result = result;
		return self;
	}
	public static Response error(Throwable e) {
		Response self = new Response();
		self.error = e.getMessage();
		if (self.error==null) {
			self.error = e.getClass().getName();
		}
		return self;
	}
}