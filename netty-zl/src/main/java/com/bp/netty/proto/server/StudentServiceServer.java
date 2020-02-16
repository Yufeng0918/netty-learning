package com.bp.netty.proto.server;

import com.bp.netty.proto.impl.StudentServiceGrpcImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by ribbo on 6/29/2017.
 */
public class StudentServiceServer {

    private Server server;

    private void start() throws IOException{
        this.server = ServerBuilder.forPort(8899).addService(new StudentServiceGrpcImpl()).build();
        this.server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("stop JVM");
            StudentServiceServer.this.stop();
        }));
        System.out.println("started");
//        System.exit(0);
    }

    private void stop(){
        if (null != this.server){
            this.server.shutdown();
        }
    }

    private void awaitTermination() throws InterruptedException {
        if (null != this.server){
            this.server.awaitTermination();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException{

        StudentServiceServer serviceServer = new StudentServiceServer();
        serviceServer.start();
        serviceServer.awaitTermination();
    }
}
