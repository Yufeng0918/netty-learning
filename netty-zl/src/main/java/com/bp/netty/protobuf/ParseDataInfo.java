package com.bp.netty.protobuf;

/**
 * Created by ribbo on 6/4/2017.
 */
public class ParseDataInfo {

    public static void main(String[] args) throws  Exception{

        DataInfo.Student student1 = DataInfo.Student.newBuilder().setName("NUS").setAge(100).setAddress("Singapore").build();
        byte[] student2ByteArr = student1.toByteArray();

        DataInfo.Student student2 = DataInfo.Student.parseFrom(student2ByteArr);
        System.out.println(student2.toString());
    }
}
