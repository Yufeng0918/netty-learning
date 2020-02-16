package com.bp.netty.proto.client;

import com.bp.netty.proto.*;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Created by ribbo on 6/29/2017.
 */
public class StudentServiceClient {

    public static void main(String[] args) throws Exception{

        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 8899).usePlaintext(true).build();
        StudentServiceGrpc.StudentServiceBlockingStub blockingStub = StudentServiceGrpc.newBlockingStub(channel);

        for(int i = 0; i < 5; i++) {
            System.out.println(blockingStub.getRealNameByUsername(MyRequest.newBuilder().setUsername("day68c").build()).getRealname());
        }

        Iterator<StudentResponse> responseIterator =  blockingStub.getStudentsByAge(StudentRequest.newBuilder().setAge(20).build());
        while (responseIterator.hasNext()){
            System.out.println(responseIterator.next().toString());
        }


        StreamObserver<StudentResponseList> studentResponseListStreamObserver = new StreamObserver<StudentResponseList>() {
            @Override
            public void onNext(StudentResponseList value) {
                value.getStudentResponseList().forEach(studentResponse -> System.out.println(studentResponse.toString()));
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed!");
            }
        };

        StudentServiceGrpc.StudentServiceStub asyncStub = StudentServiceGrpc.newStub(channel);
        StreamObserver<StudentRequest> studentRequestStreamObserver = asyncStub.getStudentListByAges(studentResponseListStreamObserver);
        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(20).build());
        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(30).build());
        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(40).build());
        studentRequestStreamObserver.onCompleted();



        StreamObserver<StreamRequest> streamRequestStreamObserver = asyncStub.biTalk(new StreamObserver<StreamResponse>() {
           @Override
           public void onNext(StreamResponse value) {
               System.out.println(value.getResponseInfo());
           }

           @Override
           public void onError(Throwable t) {
               t.printStackTrace();
           }

           @Override
           public void onCompleted() {
               System.out.println("Completed");
           }
       });


       for(int i = 0; i < 10; i++) {
           streamRequestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo(LocalDateTime.now().toString()).build());
           Thread.sleep(1000);
       }

        Thread.sleep(5000);
        channel.shutdown();
    }
}
