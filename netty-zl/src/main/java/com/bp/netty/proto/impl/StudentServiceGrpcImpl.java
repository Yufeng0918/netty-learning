package com.bp.netty.proto.impl;

import com.bp.netty.proto.*;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

/**
 * Created by ribbo on 6/29/2017.
 */
public class StudentServiceGrpcImpl extends StudentServiceGrpc.StudentServiceImplBase {

    @Override
    public void getRealNameByUsername(MyRequest request, StreamObserver<MyResponse> responseObserver) {
        System.out.println("Received: " + request.getUsername());
        responseObserver.onNext(MyResponse.newBuilder().setRealname(request.getUsername() + "Response").build());
        responseObserver.onCompleted();
    }

    @Override
    public void getStudentsByAge(StudentRequest request, StreamObserver<StudentResponse> responseObserver) {
        System.out.println("Received: " + request.getAge());
        responseObserver.onNext(StudentResponse.newBuilder().setName("Jack").setAge(28).setCity("NewYork").build());
        responseObserver.onNext(StudentResponse.newBuilder().setName("Lee").setAge(38).setCity("D.C").build());
        responseObserver.onNext(StudentResponse.newBuilder().setName("John").setAge(28).setCity("HongKong").build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<StudentRequest> getStudentListByAges(StreamObserver<StudentResponseList> responseObserver) {
        return new StreamObserver<StudentRequest>() {
            @Override
            public void onNext(StudentRequest value) {
                System.out.println("Received: " + value.getAge());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                StudentResponseList studentResponseList =  StudentResponseList.newBuilder().
                        addStudentResponse(StudentResponse.newBuilder().setName("Phl").setAge(20).setCity("NewYork").build()).
                        addStudentResponse(StudentResponse.newBuilder().setName("Igor").setAge(30).setCity("D.C").build()).
                        addStudentResponse(StudentResponse.newBuilder().setName("Nick").setAge(40).setCity("HongKong").build()).build();
                responseObserver.onNext(studentResponseList);
                responseObserver.onCompleted();
            }
        };
    }


    @Override
    public StreamObserver<StreamRequest> biTalk(StreamObserver<StreamResponse> responseObserver) {
        return new StreamObserver<StreamRequest>() {
            @Override
            public void onNext(StreamRequest value) {
                System.out.println("Received: "  +value.getRequestInfo());
                responseObserver.onNext(StreamResponse.newBuilder().setResponseInfo(UUID.randomUUID().toString()).build());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
