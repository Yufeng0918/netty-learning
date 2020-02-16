package com.bp.netty.proto.client;

import com.bp.netty.protobuf.PersonDataInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Random;

public class PersonTestClientHandler extends SimpleChannelInboundHandler<PersonDataInfo.MsgData> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PersonDataInfo.MsgData msg) throws Exception {

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        int randomInt = new Random().nextInt(3);
        PersonDataInfo.MsgData msgData;
        switch (randomInt) {
            case 0:
                 msgData = PersonDataInfo.MsgData.newBuilder().setMsgType(
                         PersonDataInfo.MsgData.MsgType.PERSON).setPerson(PersonDataInfo.Person.newBuilder().setName("Yufeng").setAge(20).setAddress("SG")).build();
                break;
            case 1:
                msgData = PersonDataInfo.MsgData.newBuilder().setMsgType(
                        PersonDataInfo.MsgData.MsgType.DOG).setDog(PersonDataInfo.Dog.newBuilder().setName("WangCai").setAge(5)).build();
                break;
            default:
                msgData = PersonDataInfo.MsgData.newBuilder().setMsgType(
                        PersonDataInfo.MsgData.MsgType.CAT).setCat(PersonDataInfo.Cat.newBuilder().setName("Amiao").setAddress("NY")).build();
                break;
        }
        ctx.channel().writeAndFlush(msgData);
    }
}
