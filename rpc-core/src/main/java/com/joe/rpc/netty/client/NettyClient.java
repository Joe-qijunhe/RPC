package com.joe.rpc.netty.client;

import com.joe.rpc.RpcClient;
import com.joe.rpc.entity.RpcRequest;
import com.joe.rpc.entity.RpcResponse;
import com.joe.rpc.enumeration.ResponseCode;
import com.joe.rpc.registry.NacosServiceRegistry;
import com.joe.rpc.registry.ServiceRegistry;
import com.joe.rpc.serializer.CommonSerializer;
import com.joe.rpc.utils.RpcFuture;
import com.joe.rpc.utils.RpcRequestHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultProgressivePromise;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient implements RpcClient {

    private final ServiceRegistry serviceRegistry;
    private long timeout;
    private long retryCount;

    public NettyClient(CommonSerializer commonSerializer, long timeout, long retryCount) {
        ChannelProvider.setCommonSerializer(commonSerializer);
        serviceRegistry = new NacosServiceRegistry();
        this.timeout = timeout;
        this.retryCount = retryCount;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) throws Throwable {
        long count = 1;
        RpcResponse rpcResponse = null;
        // 获取服务提供方的地址
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());
        String host = inetSocketAddress.getHostName();
        int port = inetSocketAddress.getPort();
        log.info("从Nacos找到服务提供方 {}:{}", host, port);
        RpcFuture<RpcResponse> rpcFuture = new RpcFuture<>(new DefaultProgressivePromise<>(new DefaultEventLoop()), timeout);
        RpcRequestHolder.REQUEST_MAP.put(rpcRequest.getSequenceId(), rpcFuture);
        // 重试机制
        while (count < retryCount) {
            try {
                // 连接并发送消息
                Channel channel = ChannelProvider.get(inetSocketAddress);
                log.info("客户端连接到服务器 {}:{}", host, port);
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                    if (future1.isSuccess()) {
                        log.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                    } else {
                        future1.channel().close();
                        log.error("发送消息时有错误发生: ", future1.cause());
                    }
                });
                // 等待响应数据返回
                rpcResponse = rpcFuture.getPromise().get(rpcFuture.getTimeout(), TimeUnit.MILLISECONDS);
                if (rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                    throw new RuntimeException("服务调用异常");
                }
                return rpcResponse.getData();
            } catch (Throwable e) {
                count++;
                //容错机制...
            }
        }
        throw new RuntimeException("rpc 调用失败，超过最大重试次数: {}" + retryCount);
    }
}
