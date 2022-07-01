/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package socktuator.discovery;

import java.lang.reflect.Parameter;

import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredOperation;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredOperationMethod;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameters;

public class DiscoveredSocktuatorOperation extends AbstractDiscoveredOperation implements SocktuatorOperation {

	private EndpointId endpointId;
	private DiscoveredOperationMethod operationMethod;
	private SocktuatorOperationParameter[] parameters;

	public DiscoveredSocktuatorOperation(EndpointId endpointId, DiscoveredOperationMethod operationMethod, OperationInvoker invoker) {
		super(operationMethod, invoker);
		this.endpointId = endpointId;
		this.operationMethod = operationMethod;
		
		Parameter[] methodParams = operationMethod.getMethod().getParameters();
		OperationParameters _params = operationMethod.getParameters();
		parameters = new SocktuatorOperationParameter[_params.getParameterCount()];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new DiscoveredSocktuatorOperationParameter(_params.get(i), methodParams[i]);	
		}
	}

	@Override
	public String getName() {
		return endpointId.toString() + "." + operationMethod.getMethod().getName();
	}

	@Override
	public Class<?> getOutputType() {
		return operationMethod.getMethod().getReturnType();
	}

	@Override
	public SocktuatorOperationParameter[] getParameters() {
		return parameters;
	}

	@Override
	public EndpointId getEndpoint() {
		return endpointId;
	}

}
