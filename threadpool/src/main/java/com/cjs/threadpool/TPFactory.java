package com.cjs.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TPFactory {
    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        for (int i = 0; i < 60; i++) {
            System.out.println("-----执行新线程:"+i+"-----");
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                    int activeCount = threadPoolExecutor.getActiveCount();
                    long taskCount = threadPoolExecutor.getTaskCount();
                    System.out.println("activeCount:"+activeCount+"  taskCount:"+taskCount);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
