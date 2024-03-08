package com.joe.rpc.client;

import com.joe.rpc.common.RpcRequest;
import com.joe.rpc.common.RpcRequestHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * RPC客户端动态代理
 * @author ziyang
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = new RpcRequest(method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes(), RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet());
        Object resObj = client.sendRequest(rpcRequest);
        if (resObj == null) {
            throw new RuntimeException("调用失败：结果为null");
        }
        return resObj;
    }
}
