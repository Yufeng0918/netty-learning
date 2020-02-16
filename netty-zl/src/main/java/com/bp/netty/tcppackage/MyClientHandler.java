package com.bp.netty.tcppackage;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.stream.IntStream;

public class MyClientHandler extends SimpleChannelInboundHandler<PersonProtocol> {

    private int count;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        IntStream.range(0, 10).forEach(i -> {
            String messageToBeSend = "send from client ";
            byte[] content = messageToBeSend.getBytes(Charset.forName("utf-8"));
            int length = content.length;

            PersonProtocol personProtocol = new PersonProtocol();
            personProtocol.setLength(length);
            personProtocol.setContent(content);

            ctx.writeAndFlush(personProtocol);
        });
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PersonProtocol msg) throws Exception {
        int length = msg.getLength();
        byte[] content = msg.getContent();

        System.out.println("Client received message: ");
        System.out.println("length: " + length);
        System.out.println("content: " + new String(content, Charset.forName("utf-8")));
        System.out.println("Client message count: " + (++this.count));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
