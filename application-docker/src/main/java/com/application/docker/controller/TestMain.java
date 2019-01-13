package com.application.docker.controller;

import com.sun.xml.internal.ws.spi.db.DatabindingProvider;
import com.sun.xml.internal.ws.util.ServiceFinder;

public class TestMain {

    public static void main(String[] args) {

         new Thread(() ->{
             for (int i = 1; i< 1000; i++) {
                 Test test1  = Test.newInstance();
                 test1.setName("thread1 "+Thread.currentThread().getName());
                 System.out.println(test1.getName());
             }
         }).start();

        new Thread(() ->{
            for (int i = 1; i< 1000; i++) {
                Test test1  = Test.newInstance();
                test1.setName("thread2 "+Thread.currentThread().getName());
                System.out.println(test1.getName());
            }
        }).start();

        for (DatabindingProvider p : ServiceFinder.find(DatabindingProvider.class)) {
            factories.add(p);
        }
        new Thread(() ->{
            for (int i = 1; i< 1000; i++) {
                Test test1  = Test.newInstance();
                test1.setName("thread3 "+Thread.currentThread().getName());
                System.out.println(test1.getName());
            }
        }).start();
        }

}
