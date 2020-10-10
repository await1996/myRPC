package com.rpc.sample.client;

import com.rpc.client.RpcProxy;
import com.rpc.sample.api.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest1 {
    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(16);
    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest1() throws ExecutionException, InterruptedException {
        HelloService helloService=rpcProxy.create(HelloService.class);
        //直接调用
        double startTime=System.currentTimeMillis();
        int cnt=100;
        while(cnt-->0){
            String res = helloService.hello("miaomiao");
            System.out.println(res+"cnt");
        }
        double endTime=System.currentTimeMillis();
        System.out.println(100/((endTime-startTime)/1000));
    }
}
