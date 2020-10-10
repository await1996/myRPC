package com.rpc.sample.api;

import java.util.concurrent.CompletableFuture;

public interface HelloService {

    String hello(String name);

    String hello(Person person);
}
