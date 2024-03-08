package com.joe.rpc.loadbalance;

import com.joe.rpc.spi.ExtensionLoader;

public class LoadBalancerFactory {
    public static LoadBalancer get(String loadBalancer) {
        String name = LoadBalancer.class.getName();
        return ExtensionLoader.getInstance().get(name + ":" + loadBalancer);
    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(LoadBalancer.class);
    }
}
