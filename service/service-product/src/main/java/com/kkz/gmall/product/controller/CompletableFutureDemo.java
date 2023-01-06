package com.kkz.gmall.product.controller;

import lombok.SneakyThrows;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureDemo {
    public static void main(String[] args) {
        // 初始化线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(50, 500,
                30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
        // 创建一个异步任务对象A
        CompletableFuture<Object> futureA = CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {
                return "404";
            }
        },threadPoolExecutor);
        // 创建一个B
        futureA.thenAcceptAsync(new Consumer<Object>() {
            @SneakyThrows
            @Override
            public void accept(Object o) {
                    Thread.sleep(500);
                    System.out.println("我是B");
            }
        }, threadPoolExecutor);
        //创建一个C
        futureA.thenAcceptAsync(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                System.out.println("我是C");
            }
        }, threadPoolExecutor);
    }
    public static void main1(String[] args) throws ExecutionException, InterruptedException {
        //创建一个没有返回值的异步对象
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//
//            System.out.println("没有返回值结果");
//        });
//        System.out.println(future.get());
        //创建一个有返回值的异步对象
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                int a =1/0;
                return 404;
            }
        }).whenComplete(new BiConsumer<Integer, Throwable>() {
            /**
             *whenComplete 和异步对象使用同一个线程
             * @param integer  异步对象执行后的返回值结果
             * @param throwable 异常对象
             */
            @Override
            public void accept(Integer integer, Throwable throwable) {

                System.out.println("whenComplete:" + integer);
                System.out.println("whenComplete:" + throwable);
            }
        }).exceptionally(new Function<Throwable, Integer>() {
            /**
             * 只处理异常的回调
             * @param throwable
             * @return
             */
            @Override
            public Integer apply(Throwable throwable) {
                return null;
            }
        }).whenCompleteAsync(new BiConsumer<Integer, Throwable>() {
            /**
             * whenCompleteAsync 根据异步对象有可能不适用统一个线程，由线程池重新分配
             * @param integer
             * @param throwable
             */
            @Override
            public void accept(Integer integer, Throwable throwable) {
            }
        });
//        System.out.println(completableFuture.get());
    }
}
