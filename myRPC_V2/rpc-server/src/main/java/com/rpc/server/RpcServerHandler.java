package com.rpc.server;

import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);

    Map<String, Object> handlerMap=null;
    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap=handlerMap;
    }

    private static Map<Integer,String>classMap =new HashMap<>();
    private static Map<Integer,String>methodMap =new HashMap<>();

    static {
        classMap.put(0, "com.rpc.sample.api.HelloService");
        //Method method=new Method();//method是final类，不能new
        methodMap.put(0,  "hello");
    }

    private static ThreadPoolExecutor threadPoolExecutor =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(16);

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        // 创建并初始化 RPC 响应对象
        RpcResponse rpcResponse=new RpcResponse();
        //rpcResponse.setRequestId(rpcRequest.getRequestId());

        //使用线程池完成耗时任务，不会阻塞主线程
        threadPoolExecutor.submit(()->{
            try{
                Object result=handle(rpcRequest);
                rpcResponse.setResult(result);
            }catch (Throwable t){
                rpcResponse.setError(t);
            }
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            // 写入 RPC 响应对象并自动关闭连接
            channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
            //System.out.println("线程池运行完毕");
        });
        //System.out.println("IO线程运行完毕");
    }

    public Object handle(RpcRequest rpcRequest) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取服务对象
        String className=classMap.get(rpcRequest.getClassID());
        Object serviceBean=handlerMap.get(className);
        //System.out.println("serviceBean"+serviceBean);
        // 获取反射调用所需的参数
        Class<?> serviceClass=serviceBean.getClass();
        String methodName = methodMap.get(rpcRequest.getMethodID());
        //System.out.println("methodName"+methodName);
        //Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        // 执行反射调用
        //如果想优化成，不需要参数类型，执行反射。。1.map直接根据方法ID获取方法而不是方法名 2.
        /*Method method = serviceClass.getMethod(methodName,parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceClass,parameters);*/
        //通过参数获取参数类型,可能导致效率低？
        //double startTime=System.currentTimeMillis();
        Class<?>[] parameterTypes=new Class[parameters.length];
        for(int i=0;i<parameters.length;i++){
            parameterTypes[i]=parameters[i].getClass();
        }
        //double endTime=System.currentTimeMillis();
        //System.out.println("getType"+(endTime*100000-startTime*100000));
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        //System.out.println("method"+serviceFastMethod);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server caught exception:",cause);
        ctx.close();
    }
}
