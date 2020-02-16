package com.bp.netty.protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Created by ribbo on 5/25/2017.
 */
public class ProtoClientChannelHandler extends SimpleChannelInboundHandler<PersonDataInfo.MsgData> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PersonDataInfo.MsgData msg) throws Exception {
        System.out.println(msg.toString());
        Thread.sleep(10000);
        ctx.writeAndFlush("From client: " + LocalDateTime.now());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        int msgType = (new Random()).nextInt(3);
        PersonDataInfo.MsgData msgData;

        switch (msgType) {
            case 0:
                msgData = PersonDataInfo.MsgData.newBuilder().setMsgType(PersonDataInfo.MsgData.MsgType.PERSON).setPerson(PersonDataInfo.Person.newBuilder().setName("Person").setAge(28).setAddress("Singapore")).build();
                break;
            case 1:
                msgData = PersonDataInfo.MsgData.newBuilder().setMsgType(PersonDataInfo.MsgData.MsgType.CAT).setCat(PersonDataInfo.Cat.newBuilder().setName("Cat").setAddress("NewYork")).build();
                break;
            default:
                msgData = PersonDataInfo.MsgData.newBuilder().setMsgType(PersonDataInfo.MsgData.MsgType.DOG).setDog(PersonDataInfo.Dog.newBuilder().setName("Dog").setAge(6)).build();
        }
        System.out.println(msgData.toString());
        ctx.writeAndFlush(msgData);
    }
}