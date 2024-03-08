package com.joe.rpc.registry;

import com.joe.rpc.spi.ExtensionLoader;

public class RegistryFactory {
    public static ServiceRegistry get(String registryService) {
        String name = ServiceRegistry.class.getName();
        return ExtensionLoader.getInstance().get(name + ":" + registryService);
    }

    public static void init() throws Exception{
        ExtensionLoader.getInstance().loadExtension(ServiceRegistry.class);
    }
}
