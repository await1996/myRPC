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
public class HelloServiceTest3 {
    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(16);
    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest1() throws ExecutionException, InterruptedException {
        HelloService helloService=rpcProxy.create(HelloService.class);

        /*int cnt=999;
        double startTime=System.currentTimeMillis();
        CompletableFuture<String> future=null;
        while(cnt-->0){
            CompletableFuture.supplyAsync(()->{
                return helloService.hello("miaomiao");
            });
        }
        future=future=CompletableFuture.supplyAsync(()->{
            return helloService.hello("miaomiao");
        });
        System.out.println(future.get());
        double endTime=System.currentTimeMillis();
        System.out.println(1000/((endTime-startTime)/1000));*/

        //future
        int cnt=999;
        double startTime1=System.currentTimeMillis();
        Future future1=null;
        while(cnt-->0){
            threadPoolExecutor.submit(()->{
                System.out.println(helloService.hello("hahaha"));
            });
        }
        future1=threadPoolExecutor.submit(()->{
            System.out.println(helloService.hello("hahaha"));
        });
        future1.get();
        //System.out.println(res+"cnt");
        double endTime1=System.currentTimeMillis();
        System.out.println(1000/((endTime1-startTime1)/1000));
    }
}
