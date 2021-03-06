package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.*;
import io.netty.example.study.client.codec.dispatcher.ClientIdleCheckHandler;
import io.netty.example.study.client.codec.dispatcher.OperationResultFuture;
import io.netty.example.study.client.codec.dispatcher.RequestPendingCentor;
import io.netty.example.study.client.codec.dispatcher.ResponseDispatcherHandler;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.OperationResult;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 14:45
 * @Description:
 */
public class ClientV2 {

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000);
        bootstrap.group(workerGroup);

        RequestPendingCentor requestPendingCentor = new RequestPendingCentor();
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocalDecoder());
                pipeline.addLast(new OrderProtocalEncoder());
                pipeline.addLast(new ResponseDispatcherHandler(requestPendingCentor));
                pipeline.addLast(new OperationToRequestMessageEncoder());
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture future = bootstrap.connect("127.0.0.1", 8090);
        future.sync();

        OperationResultFuture operationResultFuture = new OperationResultFuture();
        System.out.println(operationResultFuture.toString());
        Long stremId = IdUtil.nextId();
        requestPendingCentor.add(stremId, operationResultFuture);

        Map<Long, OperationResultFuture> resultFutureMap = new HashMap<>();
        resultFutureMap.put(stremId, operationResultFuture);
        RequestMessage requestMessage =  new RequestMessage(stremId, new OrderOperation(100, "Pomato"));
        future.channel().writeAndFlush(requestMessage);

        operationResultFuture = resultFutureMap.get(stremId);
        resultFutureMap.remove(stremId);
        System.out.println(operationResultFuture.get());
        future.channel().closeFuture().get();
    }
}
