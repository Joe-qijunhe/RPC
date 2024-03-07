package com.joe.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.joe.rpc.common.ServiceMeta;
import com.joe.rpc.common.enumeration.RpcError;
import com.joe.rpc.common.exception.RpcException;
import com.joe.rpc.loadbalance.LoadBalancer;
import com.joe.rpc.loadbalance.ServiceMetaRes;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static final NamingService namingService;

    static {
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("连接到Nacos时有错误发生: ", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    @Override
    public void register(ServiceMeta serviceMeta) {
        try {
            String serviceName = serviceMeta.getServiceName();
            String host = serviceMeta.getAddr();
            int  port = serviceMeta.getPort();
            namingService.registerInstance(serviceName, host, port);
        } catch (NacosException e) {
            log.error("注册服务时有错误发生:", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }

    @Override
    public List<ServiceMeta> discovery(String serviceName) {
        try {
            List<Instance> instances = namingService.getAllInstances(serviceName);
            List<ServiceMeta> serviceMetaList = new ArrayList<>();
            for (Instance instance : instances) {
                ServiceMeta serviceMeta = new ServiceMeta();
                serviceMeta.setServiceName(serviceName);
                serviceMeta.setAddr(instance.getIp());
                serviceMeta.setPort(instance.getPort());
                serviceMetaList.add(serviceMeta);
            }
            return serviceMetaList;
        } catch (NacosException e) {
            log.error("获取服务时有错误发生:", e);
        }
        return null;
    }
}
