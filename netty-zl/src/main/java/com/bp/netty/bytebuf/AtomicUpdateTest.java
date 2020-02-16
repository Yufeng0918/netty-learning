package com.bp.netty.bytebuf;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.stream.IntStream;

public class AtomicUpdateTest {

    public static void main(String[] args) {

        Person person1 = new Person();

        IntStream.range(0, 10).forEach(i -> {

            Thread thread = new Thread(() ->  {
                try{
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(person1.age++);
            });

//            thread.start();
        });


        Person person2 = new Person();
        AtomicIntegerFieldUpdater<Person> atomicIntegerFieldUpdater = AtomicIntegerFieldUpdater.newUpdater(Person.class, "age");

        IntStream.range(0, 10).forEach(i -> {

            Thread thread = new Thread(() ->  {
                try{
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(atomicIntegerFieldUpdater.getAndIncrement(person2));
            });

            thread.start();
        });

    }
}


class Person {

    volatile int age = 1;
}