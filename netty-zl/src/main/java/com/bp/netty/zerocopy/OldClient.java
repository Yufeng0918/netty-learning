package com.bp.netty.zerocopy;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;

public class OldClient {

    public static void main(String[] args) throws Exception{

        Socket socket = new Socket("localhost", 8899);

        String fileName = "NioTest10.txt";
        InputStream inputStream = new FileInputStream(fileName);

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());


        byte[] buffer = new byte[1024];
        long readCount ;
        long total = 0;

        long startTime = System.currentTimeMillis();

        while((readCount = inputStream.read(buffer)) >=0 ) {
            total += readCount;
            dataOutputStream.write(buffer);
        }
        System.out.println("transfer: " + total + ", time: " + (System.currentTimeMillis() - startTime));

        dataOutputStream.close();
        socket.close();
        inputStream.close();
    }
}
