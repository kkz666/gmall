package com.kkz.gmall.list.receiver;

import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListReceiver {
    @Autowired
    private SearchService searchService;
    //监听上架队列
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS, autoDelete = "false"),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperGoodsToEs(Long skuId, Message message, Channel channel) {
        try {
            //判断
            if(skuId != null){
                searchService.upperGoods(skuId);
            }
        } catch (Exception e) {
            //写入日志文件，写入数据库，对接程序员手机短信
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    //监听下架队列
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS, autoDelete = "false"),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerGoodsToEs(Long skuId, Message message, Channel channel) {
        try {
            //判断
            if(skuId != null){
                searchService.lowerGoods(skuId);
            }
        } catch (Exception e) {
            //写入日志文件 ，，写入数据库， 对接程序员手机短信
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
