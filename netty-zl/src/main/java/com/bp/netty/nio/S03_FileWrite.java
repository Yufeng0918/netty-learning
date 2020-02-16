package com.bp.netty.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class S03_FileWrite {

    public static void main(String[] args) throws Exception {

        FileInputStream fileInputStream = new FileInputStream("S13_In.txt");
        FileOutputStream fileOutputStream = new FileOutputStream("S13_Out.txt");

        FileChannel inputChannel = fileInputStream.getChannel();
        FileChannel outputChannel = fileOutputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        int read;
        while ((read = inputChannel.read(byteBuffer))!= -1) {

            byteBuffer.flip();
            outputChannel.write(byteBuffer);
            byteBuffer.clear();
        }

        inputChannel.close();
        outputChannel.close();
    }
}
