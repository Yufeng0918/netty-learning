package io.netty.example.study.client.codec.dispatcher;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.study.common.ResponseMessage;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 15:44
 * @Description:
 */
public class ResponseDispatcherHandler extends SimpleChannelInboundHandler<ResponseMessage> {

    private RequestPendingCentor requestPendingCentor;

    public ResponseDispatcherHandler(RequestPendingCentor requestPendingCentor) {
        this.requestPendingCentor = requestPendingCentor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseMessage msg) throws Exception {
        requestPendingCentor.set(msg.getMessageHeader().getStreamId(), msg.getMessageBody());

    }
}
