package com.joe.rpc.loadbalance;


import com.joe.rpc.common.ServiceMeta;
import com.joe.rpc.registry.ServiceRegistry;

import java.util.List;

public interface LoadBalancer {
    ServiceMetaRes select(String serviceName);
    void setServiceRegistry(ServiceRegistry serviceRegistry);
}
