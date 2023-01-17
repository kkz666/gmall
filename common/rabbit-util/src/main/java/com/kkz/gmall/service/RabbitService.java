package com.kkz.gmall.service;

import com.alibaba.fastjson.JSON;
import com.kkz.gmall.model.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 发送消息封装
     * @param exchange
     * @param routingKey
     * @param massage
     * @return
     */
    public boolean sendMessage(String exchange, String routingKey, Object massage){

        //创建实体类封装消息信息
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        //设置id
        String correlationDataId = UUID.randomUUID().toString().replaceAll("-", "");
        gmallCorrelationData.setId(correlationDataId);
        //设置消息
        gmallCorrelationData.setMessage(massage);
        //设置交换机
        gmallCorrelationData.setExchange(exchange);
        //设置路由
        gmallCorrelationData.setRoutingKey(routingKey);
        //存储到redis
        redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(gmallCorrelationData),
                10, TimeUnit.MINUTES);
        //发送消息,将消息对象携带过去
//        rabbitTemplate.convertAndSend(exchange,routingKey,massage);
        rabbitTemplate.convertAndSend(exchange, routingKey, massage, gmallCorrelationData);
        return true;
    }
    /**
     * 发送延迟消息
     * @param exchange
     * @param routingKey
     * @param message
     * @param delayTime
     * @return
     */
    public boolean sendDelayedMessage(String exchange, String routingKey, Object message, int delayTime){
        //重试机制-封装对象
        //创建实体类封装消息信息
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        //设置id
        String correlationDataId = UUID.randomUUID().toString().replaceAll("-", "");
        gmallCorrelationData.setId(correlationDataId);
        //设置消息
        gmallCorrelationData.setMessage(message);
        //设置交换机
        gmallCorrelationData.setExchange(exchange);
        //设置路由
        gmallCorrelationData.setRoutingKey(routingKey);
        //是否延迟
        gmallCorrelationData.setDelay(true);
        //延迟时间
        gmallCorrelationData.setDelayTime(delayTime);
        //存储到redis
        this.redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(gmallCorrelationData));
        this.rabbitTemplate.convertAndSend(exchange, routingKey, message, message1 -> {
            //设置延迟时间
            message1.getMessageProperties().setDelay(delayTime * 1000);
            return message1;
        }, gmallCorrelationData);
        return true;
    }
}
