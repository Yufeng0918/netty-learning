package com.bp.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Scanner;

/**
 * Created by ribbo on 5/28/2017.
 */
public class ChatClient {

    public static void main(String[] args) {
        EventLoopGroup worker = new NioEventLoopGroup();

        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker).channel(NioSocketChannel.class).handler(new ChatClientChannelInitializer());

            Channel channel= bootstrap.connect("localhost", 8899).sync().channel();
            Scanner sc = new Scanner(System.in);
            for(;;){
                channel.writeAndFlush(sc.nextLine() + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
