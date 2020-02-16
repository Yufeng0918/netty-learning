package com.bp.netty.zerocopy;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NewClient {

    public static void main(String[] args) throws  Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8899));
        socketChannel.configureBlocking(true);

        String fileName = "NioTest10.txt";

        FileChannel fileChannel = new FileInputStream(fileName).getChannel();
        long startTime = System.currentTimeMillis();

        long count = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
        System.out.println("time: " + (System.currentTimeMillis() - startTime));

        fileChannel.close();
    }
}
