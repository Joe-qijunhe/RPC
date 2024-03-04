package com.joe.rpc.netty.server;

import com.joe.rpc.entity.RpcRequest;
import com.joe.rpc.entity.RpcResponse;
import com.joe.rpc.enumeration.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 进行过程调用的处理器
 * @author ziyang
 */
@Slf4j
public class RequestHandler {
    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = new Object();
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (Exception e) {
            log.error("调用或发送时有错误发生：", e);
        }
        return result;
    }
}
