package io.netty.example.study.client.codec.dispatcher;

import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Auther: daiyu
 * @Date: 21/2/20 23:33
 * @Description:
 */
public class ClientIdleCheckHandler extends IdleStateHandler {

    public ClientIdleCheckHandler() {
        super(0, 5, 0);
    }
}
