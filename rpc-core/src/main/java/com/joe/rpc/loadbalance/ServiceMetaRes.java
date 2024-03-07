package com.joe.rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.joe.rpc.common.ServiceMeta;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class ServiceMetaRes {
    // 当前服务节点
    private ServiceMeta selectedService;
    // 其余服务节点，用于容错
    private List<ServiceMeta> otherService;

    public static ServiceMetaRes build(ServiceMeta selectedService, List<ServiceMeta> serviceMetas) {
        ServiceMetaRes serviceMetaRes = new ServiceMetaRes();
        serviceMetaRes.selectedService = selectedService;
        serviceMetas.remove(selectedService);
        serviceMetaRes.otherService = serviceMetas;
        return serviceMetaRes;
    }

}
