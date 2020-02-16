package com.bp.netty.grpc;

import com.bp.netty.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;

public class GrpcClient {

    public static void main(String[] args) throws Exception{

        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8899).usePlaintext(true).build();
        StudentServiceGrpc.StudentServiceBlockingStub blockingStub = StudentServiceGrpc.newBlockingStub(managedChannel);


        MyResponse myResponse = blockingStub.getRealNameByUsername(MyRequest.newBuilder().setUsername("zhangsan").build());
        System.out.println(myResponse.getRealname());

        Iterator<StudentResponse> studentResponse = blockingStub.getStudentsByAge(StudentRequest.newBuilder().setAge(20).build());
        studentResponse.forEachRemaining(i -> System.out.println(i.toString()));



        StudentServiceGrpc.StudentServiceStub stub = StudentServiceGrpc.newStub(managedChannel);
        StreamObserver<StudentRequest> studentRequestStreamObserver = stub.getStudentListByAges(new StudentStreamResponse());
        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(20).build());
        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(30).build());
        studentRequestStreamObserver.onCompleted();

        Thread.sleep(2000);


        StreamObserver<StreamRequest> streamRequestStreamObserver = stub.biTalk(new StreamObserver<StreamResponse>() {
            @Override
            public void onNext(StreamResponse value) {
                System.out.println(value.getResponseInfo());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("completed");
            }
        });
        streamRequestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo("10").build());
        streamRequestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo("20").build());
        streamRequestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo("30").build());
        streamRequestStreamObserver.onCompleted();

        Thread.sleep(5000);
    }
}


class StudentStreamResponse implements StreamObserver<StudentResponseList> {

    @Override
    public void onNext(StudentResponseList value) {
        value.getStudentResponseList().forEach(i -> System.out.println(i.getName() + " " + i.getAge()));
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(t.getMessage());
    }

    @Override
    public void onCompleted() {
        System.out.println("Completed");
    }
}
