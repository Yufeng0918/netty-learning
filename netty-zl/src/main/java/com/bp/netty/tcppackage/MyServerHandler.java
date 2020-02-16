package com.bp.netty.tcppackage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.UUID;


public class MyServerHandler extends SimpleChannelInboundHandler<PersonProtocol> {

    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PersonProtocol msg) throws Exception {

        int length = msg.getLength();
        byte[] content = msg.getContent();
        System.out.println("Server received message");
        System.out.println("length: " + length);
        System.out.println("content: " + new String(content, Charset.forName("utf-8")));

        System.out.println("Server message count " + (++this.count));

        String response = UUID.randomUUID().toString();
        int responseLength = response.getBytes("utf-8").length;
        byte[] responseContent = response.getBytes("utf-8");

        PersonProtocol personProtocol = new PersonProtocol();
        personProtocol.setLength(responseLength);
        personProtocol.setContent(responseContent);
        ctx.writeAndFlush(personProtocol);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
