package socktuator.dto;

import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Selector;


public class OperationMetadata {
	
	public static class Param {
		private String name;
		private String type;
		private boolean isMandatory;
		private Selector.Match pathParam;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public boolean isMandatory() {
			return isMandatory;
		}
		public void setMandatory(boolean isMandatory) {
			this.isMandatory = isMandatory;
		}
		public Selector.Match getPathParam() {
			return pathParam;
		}
		public void setPathParam(Selector.Match pathParam) {
			this.pathParam = pathParam;
		}
	}
	
	private String endpoint;
	private String name;
	private List<Param> params;
	private String type;
	private String outputType;
	
	public List<Param> getParams() {
		return params;
	}
	public void setParams(List<Param> params) {
		this.params = params;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public String getOutputType() {
		return outputType;
	}
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}
}
