package com.joe.test;

import com.joe.rpc.RpcClient;
import com.joe.rpc.api.HelloObject;
import com.joe.rpc.api.HelloService;
import com.joe.rpc.netty.client.NettyClient;
import com.joe.rpc.netty.client.RpcClientProxy;

public class ClientTest {
    public static void main(String[] args) {
        RpcClient client = new NettyClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
    }
}
