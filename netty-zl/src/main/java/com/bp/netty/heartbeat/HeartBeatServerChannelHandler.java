package com.bp.netty.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by ribbo on 5/21/2017.
 */
public class HeartBeatServerChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (! (evt instanceof IdleStateEvent)) {
            return;
        }

        IdleStateEvent event = (IdleStateEvent) evt;

        String eventType;

        switch (event.state()){
            case READER_IDLE:
                eventType = "READER_IDLE";
                break;
            case WRITER_IDLE:
                eventType = "WRITER_IDLE";
                break;
            default:
                eventType = "ALL_IDLE";
        }


        System.out.println(ctx.channel().remoteAddress() + " Timeout: " + eventType);
        ctx.channel().close();
    }
}
