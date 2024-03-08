package com.joe.rpc.client;

import com.joe.rpc.common.RpcResponse;
import com.joe.rpc.common.RpcFuture;
import com.joe.rpc.common.RpcRequestHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        log.info("收到服务端响应：{}", rpcResponse);
        Long sequenceId = rpcResponse.getSequenceId();
        RpcFuture<RpcResponse> rpcFuture = RpcRequestHolder.REQUEST_MAP.remove(sequenceId);
        rpcFuture.getPromise().setSuccess(rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }
}
