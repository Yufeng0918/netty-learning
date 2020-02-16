package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.*;
import io.netty.example.study.client.codec.dispatcher.RequestPendingCentor;
import io.netty.example.study.client.codec.dispatcher.ResponseDispatcherHandler;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 14:45
 * @Description:
 */
public class ClientV1 {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(workerGroup);

        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocalDecoder());
                pipeline.addLast(new OrderProtocalEncoder());
                pipeline.addLast(new OperationToRequestMessageEncoder());
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture future = bootstrap.connect("127.0.0.1", 8090);
        future.sync();
        future.channel().writeAndFlush(new OrderOperation(100, "Pomato"));
        future.channel().closeFuture().get();
    }
}
