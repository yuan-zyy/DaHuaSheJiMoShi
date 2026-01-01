package com.zyy.demo.future.demo01;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Demo01 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    System.out.println("#Run #" + (i + 1) + "...");
                }
                return 1;
            }
        });

        new Thread(futureTask).start();
        System.out.println("dddddddddddddddddd");

        Integer integer = futureTask.get();
        System.out.println("result: " + integer);
        System.out.println("yyyyyyyyyyyyyyyyyyyyyyyy");
    }

}
