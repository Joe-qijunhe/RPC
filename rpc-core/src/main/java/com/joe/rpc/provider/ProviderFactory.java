package com.joe.rpc.provider;

import com.joe.rpc.spi.ExtensionLoader;

public class ProviderFactory {
    public static ServiceProvider get(String providerService) {
        String name = ServiceProvider.class.getName();
        return ExtensionLoader.getInstance().get(name + ":" + providerService);
    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(ServiceProvider.class);
    }
}
