package com.joe.rpc.loadbalance;


import com.joe.rpc.common.ServiceMeta;
import com.joe.rpc.registry.ServiceRegistry;

import java.util.List;

public interface LoadBalancer {
    ServiceMetaRes select(String serviceName);

    static LoadBalancer getInstance(String name, ServiceRegistry serviceRegistry) {
        switch (name) {
            case "RoundRobin":
                return new RoundRobinLoadBalancer(serviceRegistry);
            default:
                return null;
        }
    }
}
