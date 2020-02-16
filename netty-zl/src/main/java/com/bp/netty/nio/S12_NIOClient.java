package com.bp.netty.nio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class S12_NIOClient {


    public static void main(String[] args) throws Exception{

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8899));

            while (true) {

                selector.select();
                Set<SelectionKey> keySet = selector.selectedKeys();

                for(SelectionKey selectionKey: keySet) {

                    if (selectionKey.isConnectable()) {
                        SocketChannel client = (SocketChannel) selectionKey.channel();

                        if (client.isConnectionPending()) {
                            client.finishConnect();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.put((LocalDateTime.now() + "connected").getBytes());
                            buffer.flip();
                            client.write(buffer);

                            ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
                            executorService.submit(() -> {
                                while (true) {
                                    try {
                                        buffer.clear();
                                        InputStreamReader input = new InputStreamReader(System.in);
                                        BufferedReader br = new BufferedReader(input);

                                        String message = br.readLine();
                                        buffer.put(message.getBytes());
                                        buffer.flip();
                                        client.write(buffer);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        SocketChannel client = (SocketChannel) selectionKey.channel();

                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        int count = client.read(readBuffer);

                        if (count > 0) {
                            String message = new String(readBuffer.array(), 0, count);
                            System.out.println(message);
                        }
                    }
                }

                keySet.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
