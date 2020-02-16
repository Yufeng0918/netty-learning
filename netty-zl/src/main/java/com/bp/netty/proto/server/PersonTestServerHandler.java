package com.bp.netty.proto.server;

import com.bp.netty.protobuf.PersonDataInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PersonTestServerHandler extends SimpleChannelInboundHandler<PersonDataInfo.MsgData> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PersonDataInfo.MsgData msg) throws Exception {
        System.out.println(msg.toString());
    }
}
