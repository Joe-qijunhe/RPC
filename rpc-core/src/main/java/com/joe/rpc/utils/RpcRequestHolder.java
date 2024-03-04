package com.joe.rpc.utils;

import com.joe.rpc.entity.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RpcRequestHolder {
    // 请求id
    public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    // 绑定请求
    public static final Map<Long, RpcFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
