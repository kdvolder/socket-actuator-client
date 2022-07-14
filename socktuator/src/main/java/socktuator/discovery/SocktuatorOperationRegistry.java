package socktuator.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.util.Assert;

import socktuator.dto.OperationMetadata;
import socktuator.dto.OperationMetadata.Param;

public class SocktuatorOperationRegistry {
	
	private static final Logger log = LoggerFactory.getLogger(SocktuatorOperationRegistry.class);

	private Map<String, SocktuatorOperation> operationsIdx = new HashMap<>();
	
	private static final SocktuatorOperationParameter[] NO_PARAMETERS = {};		
	private SocktuatorOperation getOperationsMetadataOperation = new SocktuatorOperation() {
		
		private EndpointId selfId = EndpointId.of("actuator");

		@Override
		public OperationMetadata[] invoke(InvocationContext context) {
			Collection<SocktuatorOperation> operations = operationsIdx.values();
			OperationMetadata[] datas = new OperationMetadata[operations.size()];
			int i = 0;
			for (SocktuatorOperation op : operations) {
				OperationMetadata md = new OperationMetadata();
				md.setEndpoint(op.getEndpoint().toString());
				md.setName(op.getName());
				md.setType(op.getType().toString());
				md.setOutputType(op.getOutputType().getCanonicalName());
				md.setProduces(op.getProduces());
				List<Param> params = new ArrayList<>();
				for (SocktuatorOperationParameter param : op.getParameters()) {
					Param param_md = new Param();
					param_md.setMandatory(param.isMandatory());
					param_md.setName(param.getName());
					param_md.setType(param.getType().getCanonicalName());
					param_md.setPathParam(param.getPathParam());
					params.add(param_md);
				}
				md.setParams(params);
				datas[i++] = md;
			}
			return datas;
		}
		
		@Override
		public OperationType getType() {
			return OperationType.READ;
		}
		
		@Override
		public SocktuatorOperationParameter[] getParameters() {
			return NO_PARAMETERS;
		}
		
		@Override
		public Class<?> getOutputType() {
			return OperationMetadata[].class;
		}
	
		@Override
		public String getName() {
			return getEndpoint()+"."+getEndpoint();
		}

		@Override
		public EndpointId getEndpoint() {
			return selfId;
		}

		@Override
		public List<String> getProduces() {
			return null;
		}

		@Override
		public SocktuatorOperation processAlias() {
			return this;
		}
	};

	public SocktuatorOperationRegistry(SocktuatorEndpointsSupplier endpointSupplier) {
		Collection<ExposableSocktuatorEndpoint> endpoints = endpointSupplier.getEndpoints();
		for (ExposableSocktuatorEndpoint ep : endpoints) {
			Collection<SocktuatorOperation> ops = ep.getOperations();
			for (SocktuatorOperation op : ops) {
				op = op.processAlias();
				String name = op.getName();
				Assert.isTrue(!operationsIdx.containsKey(name), "Duplicate operation with name: "+name);
				operationsIdx.put(name, op);
				log.info("actuator socket operation registered: {}", name);
			}
		}
		operationsIdx.put(getOperationsMetadataOperation.getName(), getOperationsMetadataOperation);
	}

	public SocktuatorOperation get(String op) {
		return operationsIdx.get(op);
	}
}
