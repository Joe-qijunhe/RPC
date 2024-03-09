package com.joe.rpc.server;

import com.joe.rpc.serializer.CommonSerializer;

/**
 * 服务器类通用接口
 * @author ziyang
 */
public interface RpcServer {
    void start();
    <T> void publishService(Object service, Class<T> serviceClass);
}
