package com.joe.test;

import com.joe.rpc.api.HelloObject;
import com.joe.rpc.api.HelloService;

public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(HelloObject object) {
        return "这是掉用的返回值，id=" + object.getId();
    }
}
