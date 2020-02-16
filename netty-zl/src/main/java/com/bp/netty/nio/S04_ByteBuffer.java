package com.bp.netty.nio;

import java.nio.ByteBuffer;

public class S04_ByteBuffer {

    public static void main(String[] args) {


        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.putInt(15);
        buffer.putLong(5000000L);
        buffer.putDouble(14.13);
        buffer.putChar('X');
        buffer.putShort((short) 2);


        buffer.flip();

        System.out.println(buffer.getInt());
        System.out.println(buffer.getLong());
        System.out.println(buffer.getDouble());
        System.out.println(buffer.getChar());
        System.out.println(buffer.getShort());

    }
}
