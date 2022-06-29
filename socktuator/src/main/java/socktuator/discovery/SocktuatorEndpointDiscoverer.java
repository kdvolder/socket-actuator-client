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
 * {@link EndpointDiscoverer} for {@link ExposableRscEndpoint}.
 *
 * @author Phillip Webb
 * @author Kris De Volder
 */
public class SocktuatorEndpointDiscoverer extends EndpointDiscoverer<ExposableRscEndpoint, RscOperation>
		implements RscEndpointsSupplier {

	class DiscoveredRscEndpoint extends AbstractDiscoveredEndpoint<RscOperation> implements ExposableRscEndpoint {

		DiscoveredRscEndpoint(EndpointDiscoverer<?, ?> discoverer, Object endpointBean, EndpointId id,
				boolean enabledByDefault, Collection<RscOperation> operations) {
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
			Collection<EndpointFilter<ExposableRscEndpoint>> filters) {
		super(applicationContext, parameterValueMapper, invokerAdvisors, filters);
	}

	@Override
	protected ExposableRscEndpoint createEndpoint(Object endpointBean, EndpointId id, boolean enabledByDefault,
			Collection<RscOperation> operations) {
		return new DiscoveredRscEndpoint(this, endpointBean, id, enabledByDefault, operations);
	}

	@Override
	protected RscOperation createOperation(EndpointId endpointId, DiscoveredOperationMethod operationMethod,
			OperationInvoker invoker) {
		return new DiscoveredRscOperation(endpointId, operationMethod, invoker);
	}

	@Override
	protected OperationKey createOperationKey(RscOperation operation) {
		return new OperationKey(operation.getName(), () -> "Actuator operation "+operation.getName().toString());
	}

}
