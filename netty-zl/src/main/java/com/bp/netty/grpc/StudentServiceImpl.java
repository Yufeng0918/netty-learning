package com.bp.netty.grpc;

import com.bp.netty.proto.*;
import io.grpc.stub.StreamObserver;

public class StudentServiceImpl  extends StudentServiceGrpc.StudentServiceImplBase {


    @Override
    public void getRealNameByUsername(MyRequest request, StreamObserver<MyResponse> responseObserver) {
        System.out.println("accept: " + request.getUsername());
        responseObserver.onNext(MyResponse.newBuilder().setRealname("yufeng").build());
        responseObserver.onCompleted();
    }

    @Override
    public void getStudentsByAge(StudentRequest studentRequest, StreamObserver<StudentResponse> responseStreamObserver) {
        responseStreamObserver.onNext(StudentResponse.newBuilder().setName("SG").build());
        responseStreamObserver.onNext(StudentResponse.newBuilder().setName("NY").build());
        responseStreamObserver.onNext(StudentResponse.newBuilder().setName("BJ").build());
        responseStreamObserver.onCompleted();
    }


    @Override
    public StreamObserver<StudentRequest> getStudentListByAges(StreamObserver<StudentResponseList> responseListStreamObserver) {


        return new StreamObserver<StudentRequest>() {

            StudentResponseList.Builder studentResponseListBuilder = StudentResponseList.newBuilder();

            @Override
            public void onNext(StudentRequest value) {
                System.out.println("onNext: " + value.getAge());
                studentResponseListBuilder.addStudentResponse(StudentResponse.newBuilder().setName("SG" + value.getAge()).setAge(value.getAge()));
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                StudentResponseList studentResponseList = studentResponseListBuilder.build();
                responseListStreamObserver.onNext(studentResponseList);
                responseListStreamObserver.onCompleted();
            }
        };
    }



    @Override
    public StreamObserver<StreamRequest> biTalk(StreamObserver<StreamResponse> streamResponseStreamObserver)  {

        return new StreamObserver<StreamRequest>() {
            @Override
            public void onNext(StreamRequest value) {
                streamResponseStreamObserver.onNext(StreamResponse.newBuilder().setResponseInfo("response" + value.getRequestInfo()).build());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                streamResponseStreamObserver.onCompleted();
            }
        };
    }

}
