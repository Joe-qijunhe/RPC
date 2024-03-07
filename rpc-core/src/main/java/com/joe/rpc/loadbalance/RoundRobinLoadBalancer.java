package com.joe.rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.joe.rpc.common.ServiceMeta;
import com.joe.rpc.registry.ServiceRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer{

    private static AtomicInteger roundRobinId = new AtomicInteger(0);
    private final ServiceRegistry serviceRegistry;

    public RoundRobinLoadBalancer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public ServiceMetaRes select(String serviceName) {
        List<ServiceMeta> serviceMetaList = serviceRegistry.discovery(serviceName);
        int i = roundRobinId.get();
        if (i >= serviceMetaList.size()) {
            roundRobinId.set(i % serviceMetaList.size());
        }
        ServiceMeta serviceMeta = serviceMetaList.get(i);
        roundRobinId.incrementAndGet();
        return ServiceMetaRes.build(serviceMeta, serviceMetaList);
    }
}
