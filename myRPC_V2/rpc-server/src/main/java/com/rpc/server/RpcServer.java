package com.rpc.server;

import com.rpc.common.RpcDecoder;
import com.rpc.common.RpcEncoder;
import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class RpcServer implements ApplicationContextAware, InitializingBean {
    private static final Logger LOGGER= LoggerFactory.getLogger(RpcServer.class);

    private int serverPort;

    //存放接口名到接口实现类的映射
    private Map<String,Object> handlerMap=new HashMap<String, Object>();
    public RpcServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 扫描带有 RpcService 注解的类并初始化 handlerMap 对象
        Map<String ,Object> beanMap=applicationContext.getBeansWithAnnotation(RpcService.class);
        if(!beanMap.isEmpty()){
            for(Object serviceBean:beanMap.values()){
                String interfaceName=serviceBean.getClass().
                        getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName,serviceBean);
            }
        }

    }

    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //创建并初始化Netty服务端bootstrap对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();

                    pipeline.addLast(new RpcDecoder(RpcRequest.class));
                    pipeline.addLast(new RpcEncoder(RpcResponse.class));
                    pipeline.addLast(new RpcServerHandler(handlerMap));

                }
            });
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            // 启动 RPC 服务器
            ChannelFuture channelFuture = serverBootstrap.bind("127.0.0.1", serverPort).sync();
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
