package com.joe.rpc.codec;

import com.joe.rpc.entity.RpcRequest;
import com.joe.rpc.entity.RpcResponse;
import com.joe.rpc.enumeration.PackageType;
import com.joe.rpc.enumeration.RpcError;
import com.joe.rpc.serializer.CommonSerializer;
import exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Object> {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private final CommonSerializer serializer;

    public MessageCodec(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> list) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 4字节魔数
        out.writeInt(MAGIC_NUMBER);
        // 4字节包类型
        if(msg instanceof RpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }
        // 4字节序列算法号
        out.writeInt(serializer.getCode());
        // 8字节请求序号
        if(msg instanceof RpcRequest) {
            out.writeLong( ((RpcRequest) msg).getSequenceId());
        } else {
            out.writeLong( ((RpcResponse) msg).getSequenceId());
        }

        byte[] bytes = serializer.serialize(msg);
        // 4字节正文长度
        out.writeInt(bytes.length);
        // 消息正文
        out.writeBytes(bytes);
        list.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        // 4字节魔数
        int magic = in.readInt();
        if(magic != MAGIC_NUMBER) {
            log.error("不识别的协议包: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }
        // 4字节包类型
        int packageCode = in.readInt();
        Class<?> packageClass;
        if(packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if(packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("不识别的数据包: {}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }
        // 4字节序列算法号
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if(serializer == null) {
            log.error("不识别的反序列化器: {}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }
        // 8字节请求序号
        long sequenceId = in.readLong();
        // 4字节正文长度
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object obj = serializer.deserialize(bytes, packageClass);
        list.add(obj);
    }
}
