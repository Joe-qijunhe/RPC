package com.joe.rpc;

import com.joe.rpc.serializer.CommonSerializer;

/**
 * 服务器类通用接口
 * @author ziyang
 */
public interface RpcServer {
    void start();

    void setSerializer(CommonSerializer serializer);

    <T> void publishService(Object service, Class<T> serviceClass);
}
