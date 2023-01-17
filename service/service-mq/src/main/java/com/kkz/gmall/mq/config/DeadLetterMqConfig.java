package com.kkz.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DeadLetterMqConfig {
    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";
    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(exchange_dead, true, false, null);
    }
    @Bean
    public Queue deadQueu1(){
        //设置超时
        Map<String,Object> map = new HashMap<>();
        //设置超时
        map.put("x-message-ttl",1000*3);
        //设置死信路由
        map.put("x-dead-letter-exchange",exchange_dead);
        map.put("x-dead-letter-routing-key",routing_dead_2);

        return new Queue(queue_dead_1, true, false, false, map);
    }
    @Bean
    public Queue deadQueu2(){
        return new Queue(queue_dead_2, true, false, false, null);
    }
    @Bean
    public Binding binding1(){
        return BindingBuilder.bind(deadQueu1()).to(exchange()).with(routing_dead_1);
    }
    @Bean
    public Binding binding2(){
        return BindingBuilder.bind(deadQueu2()).to(exchange()).with(routing_dead_2);
    }
}
