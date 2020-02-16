package com.bp.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.stream.IntStream;

public class ByteBufTest0 {

    public static void main(String[] args) {
        ByteBuf buffer = Unpooled.buffer(10);

        IntStream.range(0, 10).forEach(i -> buffer.writeByte(i));
        IntStream.range(0, buffer.capacity()).forEach(i -> System.out.println(buffer.getByte(i)));

    }
}
