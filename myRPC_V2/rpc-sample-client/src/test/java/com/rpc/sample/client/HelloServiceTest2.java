package com.rpc.sample.client;

import com.rpc.client.RpcProxy;
import com.rpc.sample.api.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest2 {
    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(16);
    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest1() throws ExecutionException, InterruptedException {
        HelloService helloService=rpcProxy.create(HelloService.class);
        //异步调用
        double startTime1=System.currentTimeMillis();
        int cnt=100;
        CountDownLatch countDownLatch=new CountDownLatch(100);
        while(cnt-->0){
            threadPoolExecutor.submit(()->{
                System.out.println(helloService.hello("miaomiao"));
                countDownLatch.countDown();

            });
        }
        countDownLatch.await();
        //System.out.println(res+"cnt");
        double endTime1=System.currentTimeMillis();
        System.out.println(100.0/((endTime1-startTime1)/1000));
    }
}
