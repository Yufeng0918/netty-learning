package com.bp.netty.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class S02_FileIO {

    public static void main(String[] args) throws Exception{

        FileInputStream fileInputStream = new FileInputStream("README.MD");
        FileChannel fileChannel = fileInputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        fileChannel.read(byteBuffer);

        byteBuffer.flip();

        while(byteBuffer.hasRemaining()) {
            byte b = byteBuffer.get();
            System.out.print((char)b);
        }
        fileInputStream.close();


        FileOutputStream fileOutputStream = new FileOutputStream("NioWrite.txt");
        FileChannel fileChannel1 = fileOutputStream.getChannel();
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(1024);
        byte[] message  = "hello world welcome".getBytes();
        for (int i = 0; i < message.length; i++) {
            byteBuffer1.put(message[i]);
        }

        byteBuffer1.flip();
        fileChannel1.write(byteBuffer1);
        fileOutputStream.close();

    }
}
