package com.joe.rpc.entity;

import com.joe.rpc.enumeration.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> implements Serializable {

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应状态补充信息
     */
    private String message;

    /**
     * 请求序列号
     */
    private Long sequenceId;

    /**
     * 响应数据
     */
    private T data;

    public static <T> RpcResponse<T> success(T data, Long sequenceId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        response.setSequenceId(sequenceId);
        return response;
    }

    public static <T> RpcResponse<T> fail(ResponseCode code, Long sequenceId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        response.setSequenceId(sequenceId);
        return response;
    }

}
