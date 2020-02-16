package com.bp.netty.nio;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by ribbo on 9/30/2017.
 */
public class    S13_Charset {

    public static void main(String[] args) throws Exception{


        String iFile = "S13_In.txt";
        String oFile = "S13_Out.txt";

        RandomAccessFile inputRandomAccessFile = new RandomAccessFile(iFile, "r");
        RandomAccessFile outputRandomAccessFile = new RandomAccessFile(oFile, "rw");

        long inputLength = new File(iFile).length();

        FileChannel inputFileChannel = inputRandomAccessFile.getChannel();
        FileChannel outputFileChannel = outputRandomAccessFile.getChannel();

        Charset.availableCharsets().forEach((k, v) -> System.out.println(k + "->" + v));

        MappedByteBuffer inputData = inputFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, inputLength);
        Charset charset = Charset.forName("iso-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();

        CharBuffer charBuffer = decoder.decode(inputData);
//        ByteBuffer outputData = encoder.encode(charBuffer);

        ByteBuffer outputData = Charset.forName("utf-8").encode(charBuffer);
        outputFileChannel.write(outputData);
        inputFileChannel.close();
        outputFileChannel.close();

    }
}
