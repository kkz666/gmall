package com.kkz.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConfirmReceiver {
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.comfirm", durable = "true", autoDelete = "flase"),
            exchange = @Exchange(value = "exchange.confirm", autoDelete = "false"),
            key = {"routingKey.confirm"}
    ))
    public void process(String msg, Message message, Channel channel){
        System.out.println("消费者消息内容:" + msg);
        System.out.println(new String(message.getBody()));
        //手动确认机制 参数一：消息的唯一标识 参数二：是否批量确认 false
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
