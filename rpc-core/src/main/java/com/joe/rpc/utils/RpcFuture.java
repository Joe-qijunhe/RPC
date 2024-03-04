package com.joe.rpc.utils;

import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcFuture<T> {
    private Promise<T> promise;
    private long timeout;
}
