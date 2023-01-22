package com.kkz.gmall.activity.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisChannelConfig {
    /**
     * 注册主题订阅--监听器
     * 参数一：适配器
     * 参数二：连接工厂
     * @return
     */
    @Bean
    public RedisMessageListenerContainer container(MessageListenerAdapter messageListenerAdapter, RedisConnectionFactory connectionFactory){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        //设置适配器
        container.addMessageListener(messageListenerAdapter, new PatternTopic("seckillPush"));
        //连接工厂
        container.setConnectionFactory(connectionFactory);
        return container;
    }
    //适配器
    @Bean
    public MessageListenerAdapter messageListenerAdapter(MessageReceive messageReceive){
        /**
         * 参数一：添加处理器
         * 参数二：处理器执行方法
         *  反射的方式执行处理器
         */
        return new MessageListenerAdapter(messageReceive, "receiveMessage");
    }
    //添加操作的Redistemplate
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }
}
