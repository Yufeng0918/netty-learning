package com.bp.netty.zerocopy;

import jdk.management.resource.internal.inst.ServerSocketChannelImplRMHooks;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NewServer {

    public static void main(String[] args) throws Exception{

        InetSocketAddress inetSocketAddress = new InetSocketAddress(8899);

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.setReuseAddress(true);
        serverSocketChannel.bind(inetSocketAddress);


        ByteBuffer buffer = ByteBuffer.allocate(4096);

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(true);


            int readCount = 0;

            while (-1 != readCount) {
                try {
                    readCount = socketChannel.read(buffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                buffer.rewind();
            }
        }
        
    }
}
