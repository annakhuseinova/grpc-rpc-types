package org.example.loadbalancing;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.util.List;

public class TempNameResolver extends NameResolver {

    private final String service;

    public TempNameResolver(String service) {
        this.service = service;
    }

    @Override
    public String getServiceAuthority() {
        return "temp";
    }

    // Whenever there is a resolution failure, or the server is down, the connection is broken,
    // this method will be invoked to do the resolution one more time.
    @Override
    public void refresh() {
        super.refresh();
    }

    // Whenever there is an address update, we pass the updated values to the listener. Start - means start the resolution
    @Override
    public void start(Listener2 listener) {
        List<EquivalentAddressGroup> addressGroups = ServiceRegistry.getInstances(this.service);
        ResolutionResult resolutionResult = ResolutionResult.newBuilder().setAddresses(addressGroups).build();
        listener.onResult(resolutionResult);
    }

    @Override
    public void shutdown() {

    }
}
