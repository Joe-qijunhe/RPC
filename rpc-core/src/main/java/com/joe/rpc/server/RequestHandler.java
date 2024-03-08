package com.joe.rpc.server;

import com.joe.rpc.common.RpcRequest;
import lombok.extern.slf4j.Slf4j;


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
