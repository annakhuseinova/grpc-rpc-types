package org.example.loadbalancing;

import io.grpc.EquivalentAddressGroup;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
* Just a quickly improvised service registry for load balancing examples
* */
public class ServiceRegistry {

    private static final Map<String, List<EquivalentAddressGroup>> MAP = new HashMap<>();

    // payment-service 127.0.0.1:8080m 128.0.01:8080
    public static void register(String service, List<String> instances){
        List<EquivalentAddressGroup> addressGroup = instances.stream()
                .map(instance -> instance.split(":"))
                .map(array -> new InetSocketAddress(array[0], Integer.parseInt(array[1])))
                .map(EquivalentAddressGroup::new)
                .collect(Collectors.toList());
        MAP.put(service, addressGroup);
    }

    public static List<EquivalentAddressGroup> getInstances(String service){
        return MAP.get(service);
    }
}
