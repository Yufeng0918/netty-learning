package io.netty.example.study.server.codec.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.auth.AuthOperation;
import io.netty.example.study.common.auth.AuthOperationResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: daiyu
 * @Date: 22/2/20 20:11
 * @Description:
 */
@Slf4j
@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<RequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {

        try {
            Operation operation = msg.getMessageBody();
            if (operation instanceof AuthOperation) {
                AuthOperation authOperation = AuthOperation.class.cast(operation);
                AuthOperationResult authOperationResult = authOperation.execute();
                if (authOperationResult.isPassAuth()) {
                    log.info("pass auth");
                } else {
                    log.error("failed to auth");
                    ctx.close();
                }
            } else {
                log.info("expect first msg is auth");
                ctx.close();
            }
        } catch (Exception e) {
            ctx.close();
            log.error(e.getLocalizedMessage());
        } finally {
            ctx.pipeline().remove(this);
        }


    }
}
