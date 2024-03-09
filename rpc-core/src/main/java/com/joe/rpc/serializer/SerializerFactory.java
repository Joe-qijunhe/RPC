package com.joe.rpc.serializer;

import com.joe.rpc.registry.ServiceRegistry;
import com.joe.rpc.spi.ExtensionLoader;

public class SerializerFactory {
    public static CommonSerializer get(String serializer) {
        String name = CommonSerializer.class.getName();
        return ExtensionLoader.getInstance().get(name + ":" + serializer);
    }

    public static void init() throws Exception{
        ExtensionLoader.getInstance().loadExtension(CommonSerializer.class);
    }
}
