package com.bp.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.stream.IntStream;

/**
 * Created by ribbo on 5/25/2017.
 */
public class MsgClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);
        String message = new String(buffer, Charset.forName("utf-8"));

        System.out.println("client received " + msg);
        System.out.println("client received count " + (++this.count));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        IntStream.range(0, 10).forEach(i -> {
            ByteBuf buf = Unpooled.copiedBuffer("sent from client\n", Charset.forName("utf-8"));
            ctx.writeAndFlush(buf);
        });
    }
}
