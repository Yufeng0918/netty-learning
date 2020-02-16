package com.bp.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by ribbo on 8/16/2017.
 */
public class S11_Selector {

    private static Map<String, SocketChannel> socketChannelMap = new HashMap<>();


    public static void main(String[] args) throws IOException {

        int[] ports = new int[5];

        ports[0] = 5000;
        ports[1] = 5001;
        ports[2] = 5002;
        ports[3] = 5003;
        ports[4] = 5004;


        Selector selector = Selector.open();


        for (int i = 0; i < ports.length; i++) {

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            ServerSocket serverSocket = serverSocketChannel.socket();
            InetSocketAddress address = new InetSocketAddress(ports[i]);
            serverSocket.bind(address);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("listening port: " + ports[i]);

        }

        while (true) {
            selector.select();

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {

                SelectionKey selectionKey = iterator.next();

                if (selectionKey.isAcceptable()) {

                    ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    iterator.remove();
                    System.out.println("connection from " + socketChannel);
                } else if (selectionKey.isReadable()) {

                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                    int byteRead = 0;

                    while(true) {
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        buffer.clear();

                        int read = socketChannel.read(buffer);

                        if(read <= 0) {
                            break;
                        }

                        buffer.flip();
                        socketChannel.write(buffer);
                        byteRead += read;
                    }

                    System.out.println("read: " + byteRead + ", from " + socketChannel);
                    iterator.remove();
                }
            }
        }
    }
}
