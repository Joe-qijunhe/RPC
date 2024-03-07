package com.joe.rpc.registry;

import com.joe.rpc.common.ServiceMeta;

import java.util.List;


public interface ServiceRegistry {

    void register(ServiceMeta serviceMeta);
    List<ServiceMeta> discovery(String serviceName);

}
