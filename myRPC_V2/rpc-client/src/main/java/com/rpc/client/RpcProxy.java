package com.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serviceAddress;

    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    private static Map<String,Integer> classMap =new HashMap<>();
    private static Map<String,Integer>methodMap =new HashMap<>();

    static {
        classMap.put("com.rpc.sample.api.HelloService", 0);
        methodMap.put("hello", 0);//如果直接放入HelloService.hello()，一个方法map就够了
    }


    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {

                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象并设置请求属性
                        RpcRequest request = new RpcRequest();
                        //request.setRequestId(UUID.randomUUID().toString());
                        //System.out.println(method.getDeclaringClass().getName());
                        request.setClassID(classMap.get(method.getDeclaringClass().getName()));
                        //System.out.println(method.getName());
                        request.setMethodID(methodMap.get(method.getName()));
                        request.setParameters(args);

                        // 创建 RPC 客户端对象并发送 RPC 请求
                        //double startTime=System.currentTimeMillis();
                        RpcClient client = new RpcClient("127.0.0.1", 9999);
                        //double endTime=System.currentTimeMillis();
                        //System.out.println("getType"+(endTime*1000-startTime*1000));
                        //long time = System.currentTimeMillis();
                        RpcResponse response = client.send(request);

                        if (response == null) {
                            throw new RuntimeException("response is null");
                        }
                        // 返回 RPC 响应结果
                        if (response.isError()) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }
}
