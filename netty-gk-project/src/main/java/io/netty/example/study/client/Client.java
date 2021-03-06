package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.OrderFrameDecoder;
import io.netty.example.study.client.codec.OrderFrameEncoder;
import io.netty.example.study.client.codec.OrderProtocalDecoder;
import io.netty.example.study.client.codec.OrderProtocalEncoder;
import io.netty.example.study.client.codec.dispatcher.ClientIdleCheckHandler;
import io.netty.example.study.client.codec.dispatcher.KeepLiveHandler;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.auth.AuthOperation;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.util.concurrent.ExecutionException;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 14:45
 * @Description:
 */
public class Client {

    public static void main(String[] args) throws InterruptedException, ExecutionException, SSLException {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(workerGroup);

        KeepLiveHandler keepLiveHandler = new KeepLiveHandler();

//        SslContext sslContext = SslContextBuilder.forClient().build();


        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(new ClientIdleCheckHandler());
//                pipeline.addLast(sslContext.newHandler(ch.alloc()));
                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocalDecoder());
                pipeline.addLast(new OrderProtocalEncoder());
                pipeline.addLast(keepLiveHandler);
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));

            }
        });

        ChannelFuture future = bootstrap.connect("127.0.0.1", 8090);
        future.sync();


        RequestMessage requestMessage =  new RequestMessage(IdUtil.nextId(), new AuthOperation("admin", "password"));
        future.channel().writeAndFlush(requestMessage);

        requestMessage =  new RequestMessage(IdUtil.nextId(), new OrderOperation(100, "Pomato"));
        future.channel().writeAndFlush(requestMessage);
        future.channel().closeFuture().get();
    }
}
