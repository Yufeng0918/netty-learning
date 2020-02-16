package com.bp.netty.nio;

import java.nio.ByteBuffer;

public class S06_ReadOnlyBuffer {

    public static void main(String[] args) {


        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        System.out.println(byteBuffer.getClass());

        for (int i = 0; i < byteBuffer.capacity(); i++) {
            byteBuffer.put((byte) i);
        }


        ByteBuffer buffer = byteBuffer.asReadOnlyBuffer();
        System.out.println(buffer.getClass());

        buffer.position(0);
        buffer.put((byte) 2);


    }
}
