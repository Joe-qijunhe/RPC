package com.joe.test;

import com.joe.rpc.api.HelloService;
import com.joe.rpc.netty.server.NettyServer;
import com.joe.rpc.serializer.JsonSerializer;
import com.joe.rpc.serializer.KryoSerializer;
import com.joe.rpc.serializer.ProtobufSerializer;

public class ServerTest {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer server = new NettyServer("localhost", 9999);
        server.setSerializer(new ProtobufSerializer());
        server.publishService(helloService, HelloService.class);
        server.start();
    }
}
