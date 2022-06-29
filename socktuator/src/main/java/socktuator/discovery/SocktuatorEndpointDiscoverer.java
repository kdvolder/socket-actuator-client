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

import java.util.Collection;

import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredOperationMethod;
import org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.jmx.annotation.JmxEndpointDiscoverer;
import org.springframework.context.ApplicationContext;

/**
 * {@link EndpointDiscoverer} for {@link ExposableSocktuatorEndpoint}.
 *
 * @author Phillip Webb
 * @author Kris De Volder
 */
public class SocktuatorEndpointDiscoverer extends EndpointDiscoverer<ExposableSocktuatorEndpoint, SocktuatorOperation>
		implements SocktuatorEndpointsSupplier {

	class DiscoveredSocktuatorEndpoint extends AbstractDiscoveredEndpoint<SocktuatorOperation> implements ExposableSocktuatorEndpoint {

		DiscoveredSocktuatorEndpoint(EndpointDiscoverer<?, ?> discoverer, Object endpointBean, EndpointId id,
				boolean enabledByDefault, Collection<SocktuatorOperation> operations) {
			super(discoverer, endpointBean, id, enabledByDefault, operations);
		}

	}	
	
	/**
	 * Create a new {@link JmxEndpointDiscoverer} instance.
	 * @param applicationContext the source application context
	 * @param parameterValueMapper the parameter value mapper
	 * @param invokerAdvisors invoker advisors to apply
	 * @param filters filters to apply
	 */
	public SocktuatorEndpointDiscoverer(ApplicationContext applicationContext, ParameterValueMapper parameterValueMapper,
			Collection<OperationInvokerAdvisor> invokerAdvisors,
			Collection<EndpointFilter<ExposableSocktuatorEndpoint>> filters) {
		super(applicationContext, parameterValueMapper, invokerAdvisors, filters);
	}

	@Override
	protected ExposableSocktuatorEndpoint createEndpoint(Object endpointBean, EndpointId id, boolean enabledByDefault,
			Collection<SocktuatorOperation> operations) {
		return new DiscoveredSocktuatorEndpoint(this, endpointBean, id, enabledByDefault, operations);
	}

	@Override
	protected SocktuatorOperation createOperation(EndpointId endpointId, DiscoveredOperationMethod operationMethod,
			OperationInvoker invoker) {
		return new DiscoveredSocktuatorOperation(endpointId, operationMethod, invoker);
	}

	@Override
	protected OperationKey createOperationKey(SocktuatorOperation operation) {
		return new OperationKey(operation.getName(), () -> "Actuator operation "+operation.getName().toString());
	}

}
