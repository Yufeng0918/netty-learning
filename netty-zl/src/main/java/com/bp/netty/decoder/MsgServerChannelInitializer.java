package com.bp.netty.decoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by ribbo on 5/21/2017.
 */
public class MsgServerChannelInitializer extends ChannelInitializer<SocketChannel>{


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyByteToLongDecoder2());
        pipeline.addLast(new MyLongToStringDecoder());
        pipeline.addLast(new MyLongToByteEncoder());
        pipeline.addLast(new MsgServerChannelHandler());
    }
}
