package com.bp.netty.nio;

import org.apache.commons.lang.math.IntRange;

import java.nio.IntBuffer;
import java.security.SecureRandom;

public class S01_Buffer {

    public static void main(String[] args) {

        IntBuffer buffer = IntBuffer.allocate(10);
        for(int i = 0; i < 5; i++) {
            int randomNumber = new SecureRandom().nextInt(20);
            buffer.put(randomNumber);
        }

        System.out.println("before filp limit: " + buffer.limit());
        buffer.flip();
        System.out.println("after filp limit: " + buffer.limit());


        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
    }
}
