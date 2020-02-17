package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.codec.handler.MetricHandler;
import io.netty.example.study.server.codec.handler.OrderServerProcessHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;

import java.util.concurrent.ExecutionException;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 14:45
 * @Description:
 */
public class Server {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
        serverBootstrap.childOption(NioChannelOption.TCP_NODELAY, true);
        serverBootstrap.option(NioChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.group(bossGroup, workerGroup);

        LoggingHandler debugLogHandler = new LoggingHandler(LogLevel.DEBUG);
        LoggingHandler infoLogHandler = new LoggingHandler(LogLevel.INFO);
        MetricHandler metricHandler = new MetricHandler();
        UnorderedThreadPoolEventExecutor business = new UnorderedThreadPoolEventExecutor(10, new DefaultThreadFactory("buss"));
        GlobalTrafficShapingHandler trafficShapingHandler = new GlobalTrafficShapingHandler(new NioEventLoopGroup(), 100L * 1024L * 1024L, 100L * 1024L * 1024L);


        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("debegLog", debugLogHandler);
                pipeline.addLast("trafficSharper", trafficShapingHandler);
                pipeline.addLast("frameDecoder", new OrderFrameDecoder());
                pipeline.addLast("frameEncoder", new OrderFrameEncoder());
                pipeline.addLast("protocolDecoder", new OrderProtocolDecoder());
                pipeline.addLast("protocolEncoder", new OrderProtocolEncoder());
                pipeline.addLast("metricHandler", metricHandler);
                pipeline.addLast("infoLog", infoLogHandler);
                pipeline.addLast("flushEnhance", new FlushConsolidationHandler(5, true));
                pipeline.addLast(business, "orderServer", new OrderServerProcessHandler());
            }
        });

        ChannelFuture future = serverBootstrap.bind(8090).sync();
        future.channel().closeFuture().get();
    }
}
