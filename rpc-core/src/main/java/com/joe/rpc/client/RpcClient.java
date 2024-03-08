package com.joe.rpc.client;

import com.joe.rpc.common.RpcRequest;

/**
 * 客户端类通用接口
 * @author ziyang
 */
public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest) throws Throwable;

}
