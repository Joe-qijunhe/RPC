package com.joe.rpc.netty.client;

import com.joe.rpc.RpcClient;
import com.joe.rpc.common.*;
import com.joe.rpc.common.enumeration.ResponseCode;
import com.joe.rpc.loadbalance.LoadBalancer;
import com.joe.rpc.loadbalance.ServiceMetaRes;
import com.joe.rpc.registry.NacosServiceRegistry;
import com.joe.rpc.serializer.CommonSerializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultProgressivePromise;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient implements RpcClient {

    private final LoadBalancer loadBalancer;
    private long timeout;
    private long retryCount;
    private String faultTolerantType;

    public NettyClient(int commonSerializerType, String loadBalancerType, String faultTolerantType, long timeout, long retryCount) {
        CommonSerializer commonSerializer = CommonSerializer.getByCode(commonSerializerType);
        ChannelProvider.setCommonSerializer(commonSerializer);
        loadBalancer = LoadBalancer.getInstance(loadBalancerType, new NacosServiceRegistry());
        this.faultTolerantType = faultTolerantType;
        this.timeout = timeout;
        this.retryCount = retryCount;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) throws Throwable {
        long count = 1;
        RpcResponse rpcResponse = null;
        // 获取服务提供方的地址
        ServiceMetaRes serviceMetaRes = loadBalancer.select(rpcRequest.getInterfaceName());
        // 使用负载均衡得出的服务地址
        ServiceMeta serviceMeta = serviceMetaRes.getSelectedService();
        String host = serviceMeta.getAddr();
        int port = serviceMeta.getPort();
        log.info("找到服务提供方 {}:{}", host, port);
        RpcFuture<RpcResponse> rpcFuture = new RpcFuture<>(new DefaultProgressivePromise<>(new DefaultEventLoop()), timeout);
        RpcRequestHolder.REQUEST_MAP.put(rpcRequest.getSequenceId(), rpcFuture);
        // 重试机制
        while (count < retryCount) {
            try {
                // 连接并发送消息
                Channel channel = ChannelProvider.get(serviceMeta);
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
                String errMsg = e.toString();
                // 容错机制
                switch (faultTolerantType) {
                    // 快速失败
                    case "FailFast":
                        log.warn("rpc 调用失败,触发 FailFast 策略,异常信息: {}", errMsg);
                        return null;
                    // 故障转移
                    case "Failover":
                        log.warn("rpc 调用失败,第{}次重试,异常信息:{}", count, errMsg);
                        List<ServiceMeta> otherService = serviceMetaRes.getOtherService();
                        if (!otherService.isEmpty()) {
                            serviceMeta = otherService.remove(0);
                        } else {
                            log.warn("rpc 调用失败,无服务可用 serviceName: {}, 异常信息: {}", rpcRequest.getInterfaceName(), errMsg);
                            return null;
                        }
                }
            }
        }
        throw new RuntimeException("rpc 调用失败，超过最大重试次数: {}" + retryCount);
    }
}
