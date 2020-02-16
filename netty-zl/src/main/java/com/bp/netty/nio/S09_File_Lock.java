package com.bp.netty.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by ribbo on 8/13/2017.
 */
public class S09_File_Lock {

    public static void main(String[] args) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("NioTest10", "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        FileLock fileLock = fileChannel.lock(3, 6, true);

        System.out.println(fileLock.isValid() + "\t" + fileLock.isShared());
        fileLock.release();
        randomAccessFile.close();
    }
}
