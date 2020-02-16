package com.bp.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ribbo on 5/29/2017.
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<Object> {

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Received: " + msg);
        ctx.channel().writeAndFlush(new TextWebSocketFrame("Server Time: " + LocalDateTime.now()));
    }



    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("hello");
        System.out.println("handlerAdded: " + ctx.channel().id().asLongText());
        executorService.scheduleAtFixedRate(() -> ctx.channel().writeAndFlush(new TextWebSocketFrame("hello")), 0, 3000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved: " + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
