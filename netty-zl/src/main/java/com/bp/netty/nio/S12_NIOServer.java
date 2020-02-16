package com.bp.netty.nio;

import com.google.protobuf.MapEntry;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class S12_NIOServer {

    private static Map<String, SocketChannel> clientMap = new HashMap<>();

    public static void main(String[] args) throws  Exception{

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(8899));


        Selector selector = Selector.open();
        SelectionKey selectionKeyInit = serverSocketChannel.register(selector, 0);
        selectionKeyInit.interestOps(SelectionKey.OP_ACCEPT);

        while(true) {
            try {
                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                selectionKeys.forEach(selectionKey -> {
                    SocketChannel client;
                    try {
                        if (selectionKey.isAcceptable()) {

                            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                            client = server.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                            clientMap.put(UUID.randomUUID().toString(), client);
                        } else if (selectionKey.isReadable()) {

                            client = (SocketChannel) selectionKey.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);

                            int count = client.read(buffer);

                            if (count > 0) {
                                buffer.flip();

                                Charset charset = Charset.forName("utf-8");
                                String message = String.valueOf(charset.decode(buffer).array());
                                System.out.println(client + " : " + message);


                                String senderKey = null;
                                for(Map.Entry<String, SocketChannel> entry: clientMap.entrySet()) {
                                    if (client == entry.getValue()) {
                                        senderKey = entry.getKey();
                                        break;
                                    }
                                }

                                for(Map.Entry<String, SocketChannel> entry: clientMap.entrySet()) {

                                    SocketChannel value = entry.getValue();
                                    ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                                    writeBuffer.put((senderKey + ":" + message).getBytes());
                                    writeBuffer.flip();
                                    value.write(writeBuffer);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                selectionKeys.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
