package com.joe.rpc.common;

import lombok.Data;

@Data
public class ServiceMeta {

    private String serviceName;
    private String addr;
    private int port;

}
