package com.kkz.gmall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor executor(){

        /**
         * 参数一：核心线程数
         * 参数二：最大线程数
         * 参数三：线程空闲时间
         * 参数四：时间单位
         * 参数五：阻塞队列
         * 参数六：线程工厂(使用默认)
         * 参数七：拒绝策略(使用默认)
         */
        return new ThreadPoolExecutor(
                50, 500, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000)
        );
    }
}
