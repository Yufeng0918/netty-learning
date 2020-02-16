package io.netty.example.study.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.study.common.ResponseMessage;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 14:22
 * @Description:
 */

@Slf4j
public class OrderProtocolEncoder extends MessageToMessageEncoder<ResponseMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseMessage responseMessage, List<Object> out) throws Exception {

        ByteBuf byteBuf = ctx.alloc().buffer();
        responseMessage.encode(byteBuf);
        log.info("encoding " + byteBuf);
        out.add(byteBuf);
    }
}
