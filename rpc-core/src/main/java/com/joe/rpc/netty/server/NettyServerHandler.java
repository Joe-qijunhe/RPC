package com.joe.rpc.netty.server;

import com.joe.rpc.common.RpcRequest;
import com.joe.rpc.common.RpcResponse;
import com.joe.rpc.provider.ServiceProvider;
import com.joe.rpc.provider.ServiceProviderImpl;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static RequestHandler requestHandler;
    private static ServiceProvider serviceProvider;

    static {
        requestHandler = new RequestHandler();
        serviceProvider = new ServiceProviderImpl();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        try {
            log.info("服务器接收到请求: {}", request);
            String interfaceName = request.getInterfaceName();
            Object service = serviceProvider.getServiceProvider(interfaceName);
            Object result = requestHandler.handle(request, service);
            RpcResponse<Object> response = RpcResponse.success(result, request.getSequenceId());
            ctx.writeAndFlush(response).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    log.info(String.format("服务端发送消息: %s", response));
                } else {
                    future1.channel().close();
                    log.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("处理过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }
}
