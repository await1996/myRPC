package com.rpc.sample.server;

import com.rpc.sample.api.HelloService;
import com.rpc.sample.api.Person;
import com.rpc.server.RpcService;

import java.util.concurrent.CompletableFuture;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    public String hello(String name) {
        return "hello:"+name;
    }

    public String hello(Person person) {
        return "Hello"+person.getFirstName()+" "+person.getLastName();
    }
}
