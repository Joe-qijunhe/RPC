package com.joe.rpc.server;

import com.joe.rpc.codec.MessageCodec;
import com.joe.rpc.codec.ProtocolFrameDecoder;
import com.joe.rpc.common.ServiceMeta;
import com.joe.rpc.common.enumeration.RpcError;
import com.joe.rpc.provider.ProviderFactory;
import com.joe.rpc.provider.ServiceProvider;
import com.joe.rpc.provider.ServiceProviderImpl;
import com.joe.rpc.registry.NacosServiceRegistry;
import com.joe.rpc.registry.RegistryFactory;
import com.joe.rpc.registry.ServiceRegistry;
import com.joe.rpc.serializer.CommonSerializer;
import com.joe.rpc.common.exception.RpcException;
import com.joe.rpc.serializer.ProtobufSerializer;
import com.joe.rpc.serializer.SerializerFactory;
import com.joe.rpc.spi.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer implements RpcServer {

    private final String host;
    private final int port;
    private ServiceRegistry serviceRegistry;
    private ServiceProvider serviceProvider;
    private CommonSerializer serializer;

    static {
        try {
            RegistryFactory.init();
            ProviderFactory.init();
            SerializerFactory.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化默认值
     * @param host
     * @param port
     */
    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.serializer = new ProtobufSerializer();
    }

    public NettyServer(String host, int port, String serviceRegistry, String serviceProvider, String serializerType) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = RegistryFactory.get(serviceRegistry);
        this.serviceProvider = ProviderFactory.get(serviceProvider);
        this.serializer = SerializerFactory.get(serializerType);
    }

    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service);
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceName(serviceClass.getCanonicalName());
        serviceMeta.setAddr(host);
        serviceMeta.setPort(port);
        serviceRegistry.register(serviceMeta);
    }

    @Override
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        MessageCodec messageCodec = new MessageCodec(serializer);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ProtocolFrameDecoder());
                            pipeline.addLast(loggingHandler);
                            pipeline.addLast(messageCodec);
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            Channel channel = bootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
