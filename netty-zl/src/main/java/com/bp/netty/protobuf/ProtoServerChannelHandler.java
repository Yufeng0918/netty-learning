package com.bp.netty.protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by ribbo on 6/8/2017.
 */
public class ProtoServerChannelHandler extends SimpleChannelInboundHandler<PersonDataInfo.MsgData> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PersonDataInfo.MsgData msg) throws Exception {
        System.out.println(msg.toString());
    }
}
